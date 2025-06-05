package pt.ua.tqs.voltconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VoltConnectApplication {
    public static void main(String[] args) {
        SpringApplication.run(VoltConnectApplication.class, args);
    }
} 