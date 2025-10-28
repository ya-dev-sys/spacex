package com.spacex.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpacexApplication {

    private static final Logger logger = LoggerFactory.getLogger(SpacexApplication.class);

    public static void main(String[] args) {
        logger.info("Starting spacex-backend application");
        SpringApplication.run(SpacexApplication.class, args);
    }
}
