package com.spacex.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableCaching
@EntityScan("com.spacex.launcher.model")
@EnableJpaRepositories("com.spacex.launcher.repository")
public class SpacexApplication {

    private static final Logger logger = LoggerFactory.getLogger(SpacexApplication.class);

    public static void main(String[] args) {
        logger.info("Starting spacex-backend application");
        SpringApplication.run(SpacexApplication.class, args);

    }

}
