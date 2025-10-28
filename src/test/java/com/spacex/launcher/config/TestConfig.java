package com.spacex.launcher.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.spacex.launcher")
public class TestConfig {
    // Configuration vide, l'annotation fait tout le travail
}
