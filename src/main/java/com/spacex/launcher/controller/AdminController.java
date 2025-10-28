package com.spacex.launcher.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spacex.launcher.service.LaunchService;

/**
 * Contrôleur pour les opérations d'administration
 * Accessible uniquement aux utilisateurs avec le rôle ADMIN
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final LaunchService launchService;

    public AdminController(LaunchService launchService) {
        this.launchService = launchService;
    }

    /**
     * Force la resynchronisation avec l'API SpaceX
     * Vide le cache et recharge toutes les données
     *
     * @return Nombre de lancements synchronisés
     */
    @PostMapping("/resync")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> resynchronize() {
        logger.info("Admin triggered resynchronization with SpaceX API");

        try {
            Long count = launchService.synchronizeWithSpaceX()
                    .block(); // Bloque car on veut une réponse synchrone pour l'admin

            logger.info("Resynchronization completed: {} launches processed", count);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Synchronization completed successfully",
                    "launchesProcessed", count != null ? count : 0));

        } catch (Exception e) {
            logger.error("Resynchronization failed", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Synchronization failed: " + e.getMessage()));
        }
    }
}
