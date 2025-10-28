package com.spacex.launcher.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spacex.launcher.dto.AuthRequest;
import com.spacex.launcher.dto.AuthResponse;
import com.spacex.launcher.security.JwtUtil;

import jakarta.validation.Valid;

/**
 * Contrôleur d'authentification
 * Endpoint public (pas besoin de JWT)
 *
 * IMPORTANT: Pas de préfixe /api car géré par server.servlet.context-path
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Endpoint de connexion
     * POST /auth/login
     * Body: { "email": "user@example.com", "password": "password" }
     *
     * @return JWT token si authentification réussie
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        logger.info("Login attempt for email: {}", request.email());

        try {
            // Authentification avec Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()));

            // Charger les détails de l'utilisateur
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());

            // Générer le JWT
            String token = jwtUtil.generateToken(userDetails);

            logger.info("Login successful for user: {} with roles: {}",
                    userDetails.getUsername(),
                    userDetails.getAuthorities());

            return ResponseEntity.ok(new AuthResponse(token, "Bearer"));

        } catch (AuthenticationException ex) {
            logger.warn("Authentication failed for email: {} - {}",
                    request.email(),
                    ex.getClass().getSimpleName());

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", "Unauthorized",
                            "message", "Invalid credentials"));

        } catch (Exception ex) {
            logger.error("Unexpected error during authentication for: {}",
                    request.email(), ex);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Internal Server Error",
                            "message", "An unexpected error occurred"));
        }
    }
}
