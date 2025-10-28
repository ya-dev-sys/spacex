package com.spacex.launcher;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import com.spacex.launcher.config.TestConfig;

@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
class SpacexApplicationTests {

    @Test
    void contextLoads() {
        // Test vide pour vérifier que le contexte se charge correctement
    }
}
