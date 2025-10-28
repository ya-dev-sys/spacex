package com.spacex.launcher.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spacex.launcher.dto.LaunchStats;
import com.spacex.launcher.dto.YearlyStats;
import com.spacex.launcher.model.Launch;
import com.spacex.launcher.service.LaunchService;

/**
 * Contrôleur pour le tableau de bord SpaceX
 * Accessible aux utilisateurs authentifiés (USER et ADMIN)
 *
 * IMPORTANT: Pas de préfixe /api car géré par server.servlet.context-path
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    private final LaunchService launchService;

    public DashboardController(LaunchService launchService) {
        this.launchService = launchService;
    }

    /**
     * Récupère les KPIs globaux
     * GET /dashboard/kpis
     */
    @GetMapping("/kpis")
    public ResponseEntity<LaunchStats> getKpis(Authentication authentication) {
        logger.debug("User '{}' fetching KPIs", authentication.getName());
        return ResponseEntity.ok(launchService.getGlobalStats());
    }

    /**
     * Récupère les statistiques par année
     * GET /dashboard/stats/yearly
     */
    @GetMapping("/stats/yearly")
    public ResponseEntity<List<YearlyStats>> getYearlyStats(Authentication authentication) {
        logger.debug("User '{}' fetching yearly stats", authentication.getName());
        return ResponseEntity.ok(launchService.getYearlyStats());
    }

    /**
     * Récupère la liste paginée des lancements avec filtres optionnels
     * GET /dashboard/launches?year=2023&success=true&page=0&size=10
     *
     * @param year     Filtre par année (optionnel)
     * @param success  Filtre par statut de succès (optionnel)
     * @param pageable Pagination (par défaut: page 0, size 10, tri par date DESC)
     */
    @GetMapping("/launches")
    public ResponseEntity<Page<Launch>> getLaunches(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Boolean success,
            @PageableDefault(size = 10, sort = "dateUtc", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        logger.info("User '{}' fetching launches (year={}, success={}, page={})",
                authentication.getName(), year, success, pageable.getPageNumber());

        // Filtre par année
        if (year != null) {
            return ResponseEntity.ok(launchService.getLaunchesByYear(year, pageable));
        }

        // Filtre par statut
        if (success != null) {
            return ResponseEntity.ok(launchService.getLaunchesByStatus(success, pageable));
        }

        // Tous les lancements
        return ResponseEntity.ok(launchService.getAllLaunches(pageable));
    }

    /**
     * Récupère le détail d'un lancement
     * GET /dashboard/launches/{id}
     */
    @GetMapping("/launches/{id}")
    public ResponseEntity<Launch> getLaunchDetail(
            @PathVariable String id,
            Authentication authentication) {

        logger.debug("User '{}' fetching launch detail for id: {}",
                authentication.getName(), id);

        return launchService.getLaunchById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
