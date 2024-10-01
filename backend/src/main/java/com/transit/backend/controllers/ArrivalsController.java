package com.transit.backend.controllers;

import com.transit.backend.domain.ArrivalsService;
import com.transit.backend.models.Stop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class ArrivalsController {

    private static final Logger log = LoggerFactory.getLogger(ArrivalsController.class);

    private final ArrivalsService arrivalsService;

    public ArrivalsController(ArrivalsService arrivalsService) {
        this.arrivalsService = arrivalsService;
    }

    @GetMapping("/arrivals")
    public ResponseEntity<Map<String, Object>> getAllStops() {
        Map<String, Stop> stops = arrivalsService.getAllStops();
        long runCount = arrivalsService.getRunCount();
        log.info("Sending {} stops, current run: {}", stops.size(), runCount);

        Map<String, Object> response = new HashMap<>();
        response.put("stops", stops);
        response.put("timestamp", Instant.now().toString());
        response.put("runCount", runCount);

        return ResponseEntity.ok(response);
    }
}