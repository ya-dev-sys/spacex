package com.spacex.launcher.config;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.spacex.launcher.model.Role;
import com.spacex.launcher.model.Userx;
import com.spacex.launcher.repository.RoleRepository;
import com.spacex.launcher.repository.UserRepository;
import com.spacex.launcher.service.LaunchService;

/**
 * Initialise les données au démarrage de l'application
 * 1. Crée les rôles (ADMIN, USER)
 * 2. Crée les utilisateurs par défaut
 * 3. Synchronise avec l'API SpaceX
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final LaunchService launchService;

    public DataInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            LaunchService launchService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.launchService = launchService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("=================================");
        logger.info("Starting data initialization");
        logger.info("=================================");

        // 1. Créer les rôles
        initializeRoles();

        // 2. Créer les utilisateurs
        initializeUsers();

        // 3. Synchroniser avec SpaceX API
        synchronizeSpaceXData();

        logger.info("=================================");
        logger.info("Data initialization completed");
        logger.info("=================================");
    }

    private void initializeRoles() {
        logger.info("Step 1: Initializing roles");

        Role roleAdmin = roleRepository.findByName("ROLE_ADMIN");
        if (roleAdmin == null) {
            roleAdmin = new Role();
            roleAdmin.setName("ROLE_ADMIN");
            roleAdmin = roleRepository.save(roleAdmin);
            logger.info("✓ Created role: ROLE_ADMIN");
        } else {
            logger.info("✓ Role ROLE_ADMIN already exists");
        }

        Role roleUser = roleRepository.findByName("ROLE_USER");
        if (roleUser == null) {
            roleUser = new Role();
            roleUser.setName("ROLE_USER");
            roleUser = roleRepository.save(roleUser);
            logger.info("✓ Created role: ROLE_USER");
        } else {
            logger.info("✓ Role ROLE_USER already exists");
        }
    }

    @Transactional
    private void initializeUsers() {
        logger.info("Step 2: Initializing users");

        Role roleAdmin = roleRepository.findByName("ROLE_ADMIN");
        Role roleUser = roleRepository.findByName("ROLE_USER");

        if (roleAdmin == null || roleUser == null) {
            logger.error("Roles not found. Please ensure roles are initialized first.");
            return;
        }

        // Admin user
        createUserIfNotExists(
                "admin",
                "admin@example.com",
                "admin123",
                Set.of(roleAdmin, roleUser));

        // Regular user
        createUserIfNotExists(
                "user",
                "user@example.com",
                "user123",
                Set.of(roleUser));

        // Vérification finale
        long userCount = userRepository.count();
        logger.info("Total users in database: {}", userCount);

        if (userCount == 0) {
            logger.error("No users were created! Database is empty.");
            logger.error("Please check database connection and permissions.");
        }
    }

    @Transactional
    private void createUserIfNotExists(String username, String email, String password, Set<Role> roles) {
        if (!userRepository.findByEmail(email).isPresent()) {
            Userx user = Userx.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .roles(roles)
                    .build();

            try {
                userRepository.save(user);
                userRepository.flush();
                logger.info("✓ Created user: {}", username);
            } catch (Exception e) {
                logger.error("Failed to create user {}: {}", username, e.getMessage());
            }
        } else {
            logger.info("✓ User already exists: {}", username);
        }
    }

    private void synchronizeSpaceXData() {
        logger.info("Step 3: Synchronizing with SpaceX API");

        try {
            Long count = launchService.synchronizeWithSpaceX()
                    .block(); // Block car on est dans le démarrage

            logger.info("✓ SpaceX synchronization completed: {} launches processed", count);

        } catch (Exception e) {
            logger.error("✗ SpaceX synchronization failed: {}", e.getMessage());
            logger.warn("Application will start but dashboard will be empty");
            logger.warn("Admin can trigger manual resync via POST /api/admin/resync");
        }
    }
}
