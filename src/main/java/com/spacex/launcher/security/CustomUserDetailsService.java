package com.spacex.launcher.security;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.spacex.launcher.model.Role;
import com.spacex.launcher.model.Userx;
import com.spacex.launcher.repository.UserRepository;

/**
 * Service de chargement des détails utilisateur pour Spring Security
 *
 * OPTIMISATIONS:
 * - Utilisation de findByUsernameOrEmailWithRoles (1 seule requête avec JOIN
 * FETCH)
 * - Gestion des Optional au lieu de null
 * - Authentification flexible (username OU email)
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Charge un utilisateur par username OU email
     * Appelé par Spring Security lors de l'authentification
     *
     * ✅ OPTIMISATION: 1 seule requête SQL avec JOIN FETCH des rôles
     *
     * @param identifier Username ou email
     * @return UserDetails avec authorities
     * @throws UsernameNotFoundException si l'utilisateur n'existe pas
     */
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        logger.debug("Loading user by identifier: '{}'", identifier);

        // ✅ Recherche flexible: username OU email en 1 seule requête
        Userx user = userRepository.findByUsernameOrEmailWithRoles(identifier)
                .orElseThrow(() -> {
                    String message = String.format("User not found: %s", identifier);
                    logger.error(message);
                    return new UsernameNotFoundException(message);
                });

        // Vérifier que l'utilisateur a des rôles
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            String message = String.format("User '%s' has no roles assigned", identifier);
            logger.error(message);
            throw new UsernameNotFoundException(message);
        }

        // Convertir les rôles en authorities Spring Security
        var authorities = user.getRoles().stream()
                .map(Role::getName)
                .map(roleName -> {
                    // S'assurer que le rôle commence par ROLE_
                    String authority = roleName.startsWith("ROLE_")
                            ? roleName
                            : "ROLE_" + roleName;
                    logger.trace("Mapped role '{}' to authority '{}'", roleName, authority);
                    return authority;
                })
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        logger.info("Successfully loaded user '{}' with {} roles: {}",
                user.getUsername(),
                authorities.size(),
                authorities.stream()
                        .map(a -> a.getAuthority())
                        .collect(Collectors.joining(", ")));

        // Construire le UserDetails Spring Security
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
