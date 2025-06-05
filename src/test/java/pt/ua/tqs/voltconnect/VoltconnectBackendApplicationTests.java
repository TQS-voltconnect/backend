package pt.ua.tqs.voltconnect;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Use the 'test' profile for testing purposes
class VoltConnectApplicationTests {

    @Test
    void contextLoads() {
        /*
         * This test method is intentionally empty as it only verifies that the Spring context
         * loads successfully. The @SpringBootTest annotation causes Spring to attempt to start
         * the application context - if this succeeds, the test passes. This is a basic smoke
         * test to ensure our Spring configuration is valid.
         */
    }
}