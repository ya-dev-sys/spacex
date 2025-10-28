package com.spacex.launcher.client;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.spacex.launcher.dto.spacex.LaunchDto;
import com.spacex.launcher.dto.spacex.LaunchPadDto;
import com.spacex.launcher.dto.spacex.RocketDto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Client pour l'API publique SpaceX v5
 * Documentation: https://github.com/r-spacex/SpaceX-API/tree/master/docs/v4
 */
@Service
public class SpaceXClient {
        private static final Logger logger = LoggerFactory.getLogger(SpaceXClient.class);
        private static final int TIMEOUT_SECONDS = 30;

        private final WebClient webClient;

        public SpaceXClient(@Value("${spacex.api.base-url:https://api.spacexdata.com}") String baseUrl) {
                this.webClient = WebClient.builder()
                                .baseUrl(baseUrl)
                                .build();
                logger.info("SpaceXClient initialized with base URL: {}", baseUrl);
        }

        /**
         * Récupère tous les lancements depuis l'API SpaceX v5
         *
         * @return Flux de LaunchDto
         */
        public Flux<LaunchDto> getAllLaunches() {
                logger.debug("Fetching all launches from SpaceX API");
                return webClient.get()
                                .uri("/v5/launches")
                                .retrieve()
                                .bodyToFlux(LaunchDto.class)
                                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                                .doOnComplete(() -> logger.info("Successfully fetched all launches"))
                                .doOnError(WebClientResponseException.class,
                                                error -> logger.error(
                                                                "HTTP error fetching launches: status={}, body={}",
                                                                error.getStatusCode(), error.getResponseBodyAsString()))
                                .doOnError(error -> logger.error("Error fetching launches", error));
        }

        /**
         * Récupère les détails d'une fusée
         *
         * @param rocketId ID de la fusée
         * @return Mono de RocketDto
         */
        public Mono<RocketDto> getRocket(String rocketId) {
                logger.debug("Fetching rocket details for ID: {}", rocketId);
                return webClient.get()
                                .uri("/v4/rockets/{id}", rocketId)
                                .retrieve()
                                .bodyToMono(RocketDto.class)
                                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                                .doOnSuccess(rocket -> logger.debug("Successfully fetched rocket: {}",
                                                rocket != null ? rocket.getName() : "null"))
                                .doOnError(error -> logger.error("Error fetching rocket {}", rocketId, error));
        }

        /**
         * Récupère les détails d'un site de lancement
         *
         * @param launchPadId ID du launchpad
         * @return Mono de LaunchPadDto
         */
        public Mono<LaunchPadDto> getLaunchPad(String launchPadId) {
                logger.debug("Fetching launchpad details for ID: {}", launchPadId);
                return webClient.get()
                                .uri("/v4/launchpads/{id}", launchPadId)
                                .retrieve()
                                .bodyToMono(LaunchPadDto.class)
                                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                                .doOnSuccess(pad -> logger.debug("Successfully fetched launchpad: {}",
                                                pad != null ? pad.getName() : "null"))
                                .doOnError(error -> logger.error("Error fetching launchpad {}", launchPadId, error));
        }
}
