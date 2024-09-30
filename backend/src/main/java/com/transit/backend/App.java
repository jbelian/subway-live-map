package com.transit.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

//    @Value("${transiter.url}")
//    private String transiterUrl;
//
//    @PostConstruct
//    public void init() {
//        log.info("Transiter URL: {}", transiterUrl);
//        try {
//            ResponseEntity<String> response = restTemplate.getForEntity(transiterUrl, String.class);
//            log.info("Transiter connection test: {}", response.getStatusCode());
//        } catch (Exception e) {
//            log.error("Failed to connect to Transiter: {}", e.getMessage());
//        }
//    }
}