package com.trackflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test to verify Spring application context loads successfully.
 *
 * <p>Uses the 'test' profile which configures H2 in-memory database
 * instead of PostgreSQL, so tests can run without external dependencies.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
class TrackFlowApplicationTests {

    @Test
    void contextLoads() {
        // If this test passes, it means:
        // 1. All beans are properly configured
        // 2. No circular dependencies
        // 3. All configurations are valid
    }

}
