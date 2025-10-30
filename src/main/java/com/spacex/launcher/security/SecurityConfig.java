package com.spacex.launcher.security;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuration de la sécurité Spring Security avec JWT
 *
 * IMPORTANT: Cette configuration suppose que le context-path est configuré dans
 * application.properties
 * Si context-path = /api, alors les URLs seront /api/auth/login,
 * /api/dashboard/kpis, etc.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtFilter jwtFilter;
    private final JwtAuthenticationEntryPoint authEntryPoint;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(
            JwtFilter jwtFilter,
            JwtAuthenticationEntryPoint authEntryPoint,
            CustomUserDetailsService customUserDetailsService) {
        this.jwtFilter = jwtFilter;
        this.authEntryPoint = authEntryPoint;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.debug("Creating PasswordEncoder (BCrypt with strength 10)");
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        logger.debug("Creating DaoAuthenticationProvider");
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        logger.debug("Providing AuthenticationManager");
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configuration CORS pour permettre les requêtes depuis Angular
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000", // next js dev
                "http://localhost:8080" // Backend (si nécessaire)
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        logger.info("CORS configured for origins: {}", configuration.getAllowedOrigins());
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring SecurityFilterChain");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Routes publiques (sans préfixe /api)
                        .requestMatchers("/auth/**", "/actuator/**", "/error").permitAll()

                        // Routes protégées ADMIN uniquement
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Routes protégées USER ou ADMIN
                        .requestMatchers("/dashboard/**").hasAnyRole("USER", "ADMIN")

                        // Toute autre requête nécessite une authentification
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider());

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        logger.info("SecurityFilterChain configured successfully");
        logger.debug("Public endpoints: /auth/**, /actuator/**, /error");
        logger.debug("Admin endpoints: /admin/** (ROLE_ADMIN required)");
        logger.debug("Dashboard endpoints: /dashboard/** (ROLE_USER or ROLE_ADMIN required)");

        return http.build();
    }
}
