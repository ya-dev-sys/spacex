package com.spacex.launcher.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtre JWT qui intercepte chaque requête pour valider le token
 * S'exécute une seule fois par requête (OncePerRequestFilter)
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String header = request.getHeader("Authorization");
        final String requestURI = request.getRequestURI();

        logger.trace("Processing request: {} {}", request.getMethod(), requestURI);

        final String token = jwtUtil.stripPrefix(header);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String username = jwtUtil.extractUsername(token);

                if (username != null) {
                    logger.debug("Token found for user: {}", username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtUtil.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());

                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        logger.debug("Authentication set for user: {} with authorities: {}",
                                username, userDetails.getAuthorities());
                    } else {
                        logger.warn("Invalid token for user: {}", username);
                    }
                }
            } catch (Exception ex) {
                logger.error("JWT processing error for {}: {}", requestURI, ex.getMessage());
            }
        } else if (token == null) {
            logger.trace("No JWT token found in request to {}", requestURI);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Détermine si le filtre doit être ignoré pour certains endpoints
     * Les endpoints publics ne nécessitent pas de validation JWT
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();

        // Enlever le context-path si présent
        if (!contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        boolean shouldSkip = path.startsWith("/auth/") ||
                path.startsWith("/actuator/") ||
                path.equals("/error");

        if (shouldSkip) {
            logger.trace("Skipping JWT filter for public endpoint: {}", path);
        }

        return shouldSkip;
    }
}
