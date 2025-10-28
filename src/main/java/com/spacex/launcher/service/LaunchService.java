package com.spacex.launcher.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spacex.launcher.client.SpaceXClient;
import com.spacex.launcher.dto.LaunchStats;
import com.spacex.launcher.dto.YearlyStats;
import com.spacex.launcher.dto.spacex.LaunchDto;
import com.spacex.launcher.mapper.LaunchMapper;
import com.spacex.launcher.model.Launch;
import com.spacex.launcher.model.LaunchPad;
import com.spacex.launcher.model.Rocket;
import com.spacex.launcher.repository.LaunchPadRepository;
import com.spacex.launcher.repository.LaunchRepository;
import com.spacex.launcher.repository.RocketRepository;

import reactor.core.publisher.Mono;

/**
 * Service métier pour la gestion des lancements SpaceX
 */
@Service
public class LaunchService {
    private static final Logger logger = LoggerFactory.getLogger(LaunchService.class);

    private final LaunchRepository launchRepository;
    private final RocketRepository rocketRepository;
    private final LaunchPadRepository launchPadRepository;
    private final SpaceXClient spaceXClient;
    private final LaunchMapper launchMapper;

    public LaunchService(
            LaunchRepository launchRepository,
            RocketRepository rocketRepository,
            LaunchPadRepository launchPadRepository,
            SpaceXClient spaceXClient,
            LaunchMapper launchMapper) {
        this.launchRepository = launchRepository;
        this.rocketRepository = rocketRepository;
        this.launchPadRepository = launchPadRepository;
        this.spaceXClient = spaceXClient;
        this.launchMapper = launchMapper;
    }

    /**
     * Synchronise les données avec l'API SpaceX
     * Utilisé au démarrage et par l'admin
     */
    @Transactional
    @CacheEvict(value = { "launchStats", "yearlyStats" }, allEntries = true)
    public Mono<Long> synchronizeWithSpaceX() {
        logger.info("Starting synchronization with SpaceX API");

        return spaceXClient.getAllLaunches()
                .flatMap(this::enrichAndSaveLaunch)
                .count()
                .doOnSuccess(count -> logger.info("Synchronization completed: {} launches processed", count))
                .doOnError(error -> logger.error("Synchronization failed", error));
    }

    /**
     * Enrichit un LaunchDto avec Rocket et LaunchPad puis sauvegarde
     */
    private Mono<Launch> enrichAndSaveLaunch(LaunchDto dto) {
        return Mono.fromCallable(() -> {
            // Récupérer ou créer la fusée
            Rocket rocket = null;
            if (dto.getRocket() != null) {
                rocket = rocketRepository.findById(dto.getRocket())
                        .orElseGet(() -> fetchAndSaveRocket(dto.getRocket()));
            }

            // Récupérer ou créer le launchpad
            LaunchPad launchPad = null;
            if (dto.getLaunchpad() != null) {
                launchPad = launchPadRepository.findById(dto.getLaunchpad())
                        .orElseGet(() -> fetchAndSaveLaunchPad(dto.getLaunchpad()));
            }

            // Mapper et sauvegarder le lancement
            Launch launch = launchMapper.toEntity(dto, rocket, launchPad);
            return launchRepository.save(launch);
        });
    }

    private Rocket fetchAndSaveRocket(String rocketId) {
        try {
            return spaceXClient.getRocket(rocketId)
                    .map(dto -> {
                        Rocket rocket = launchMapper.toEntity(dto);
                        return rocketRepository.save(rocket);
                    })
                    .block();
        } catch (Exception e) {
            logger.warn("Failed to fetch rocket {}, using placeholder", rocketId, e);
            return createPlaceholderRocket(rocketId);
        }
    }

    private LaunchPad fetchAndSaveLaunchPad(String launchPadId) {
        try {
            return spaceXClient.getLaunchPad(launchPadId)
                    .map(dto -> {
                        LaunchPad pad = launchMapper.toEntity(dto);
                        return launchPadRepository.save(pad);
                    })
                    .block();
        } catch (Exception e) {
            logger.warn("Failed to fetch launchpad {}, using placeholder", launchPadId, e);
            return createPlaceholderLaunchPad(launchPadId);
        }
    }

    private Rocket createPlaceholderRocket(String id) {
        Rocket rocket = Rocket.builder()
                .id(id)
                .name("Unknown Rocket")
                .type("Unknown")
                .active(false)
                .build();
        return rocketRepository.save(rocket);
    }

    private LaunchPad createPlaceholderLaunchPad(String id) {
        LaunchPad pad = LaunchPad.builder()
                .id(id)
                .name("Unknown Launch Pad")
                .build();
        return launchPadRepository.save(pad);
    }

    @Cacheable(value = "launchStats")
    public LaunchStats getGlobalStats() {
        logger.debug("Calculating global launch statistics");

        // ✅ Optimisation: Utiliser COUNT SQL au lieu de charger en mémoire
        long totalLaunches = launchRepository.count();
        long successfulLaunches = launchRepository.countSuccessfulLaunches();

        // ✅ Optimisation: Méthode dédiée avec JOIN FETCH
        Launch nextLaunch = launchRepository
                .findNextLaunch(Instant.now())
                .orElse(null);

        double successRate = totalLaunches > 0
                ? (double) successfulLaunches / totalLaunches * 100
                : 0;

        logger.debug("Global stats: total={}, successRate={:.2f}%", totalLaunches, successRate);
        return new LaunchStats(totalLaunches, successRate, nextLaunch);
    }

    @Cacheable(value = "yearlyStats")
    public List<YearlyStats> getYearlyStats() {
        logger.debug("Calculating yearly statistics");

        // ✅ Optimisation: Utiliser SQL GROUP BY au lieu de Java stream
        // Récupérer toutes les années distinctes
        List<Launch> allLaunches = launchRepository.findAll();

        return allLaunches.stream()
                .map(launch -> LocalDateTime.ofInstant(launch.getDateUtc(), ZoneId.systemDefault()).getYear())
                .distinct()
                .sorted()
                .map(year -> {
                    Instant start = LocalDateTime.of(year, 1, 1, 0, 0)
                            .atZone(ZoneId.systemDefault()).toInstant();
                    Instant end = LocalDateTime.of(year, 12, 31, 23, 59, 59)
                            .atZone(ZoneId.systemDefault()).toInstant();

                    // ✅ Utiliser les méthodes de comptage SQL optimisées
                    long total = launchRepository.countByYear(start, end);
                    long successful = launchRepository.countSuccessfulByYear(start, end);
                    double successRate = total > 0 ? (double) successful / total * 100 : 0;

                    YearlyStats stats = new YearlyStats(total, successRate);
                    stats.setYear(year);
                    return stats;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<Launch> getAllLaunches(Pageable pageable) {
        logger.debug("Fetching all launches with pagination: {}", pageable);
        // ✅ Utiliser findAllWithDetails pour éviter N+1
        return launchRepository.findAllWithDetails(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Launch> getLaunchesByYear(int year, Pageable pageable) {
        logger.debug("Fetching launches for year: {}", year);
        Instant startOfYear = LocalDateTime.of(year, 1, 1, 0, 0)
                .atZone(ZoneId.systemDefault()).toInstant();
        Instant endOfYear = LocalDateTime.of(year, 12, 31, 23, 59, 59)
                .atZone(ZoneId.systemDefault()).toInstant();

        // ✅ Utiliser findByYearWithDetails pour éviter N+1
        return launchRepository.findByYearWithDetails(startOfYear, endOfYear, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Launch> getLaunchesByStatus(Boolean success, Pageable pageable) {
        logger.debug("Fetching launches by success status: {}", success);
        // ✅ Utiliser findBySuccessWithDetails pour éviter N+1
        return launchRepository.findBySuccessWithDetails(success, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Launch> getLaunchById(String id) {
        logger.debug("Fetching launch by id: {}", id);
        // ✅ Utiliser findByIdWithDetails pour charger aussi les payloads
        return launchRepository.findByIdWithDetails(id);
    }
}
