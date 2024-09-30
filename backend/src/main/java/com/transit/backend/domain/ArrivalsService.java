package com.transit.backend.domain;

import com.transit.backend.models.Arrival;
import com.transit.backend.models.Stop;
import com.transit.backend.models.StopTime;
import com.transit.backend.models.TransiterResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ArrivalsService {
    private static final Logger log = LoggerFactory.getLogger(ArrivalsService.class);

    // smoothing factor for Moving Average: smoother closer to 0 and more reactive closer to 1
    private static final double ALPHA = 0.25;

    // outer key is platformId, inner key is lineId
    private final Map<String, Map<String, Arrival>> persistentArrivals = new HashMap<>();

    private final RestTemplate restTemplate;
    private final String transiterUrl;
    private volatile Map<String, Stop> allStops = new HashMap<>();

    private final AtomicLong runCount = new AtomicLong(0);

    public ArrivalsService(RestTemplate restTemplate,
                           @Value("${transiter.url}") String transiterUrl) {
        this.restTemplate = restTemplate;
        this.transiterUrl = transiterUrl;
    }

    public Map<String, Stop> getAllStops() {
        return new HashMap<>(allStops); // Return a copy to ensure thread safety
    }

    @Scheduled(fixedRate = 30000)
    public void updateStops() {
        Map<String, Stop> newStops = new HashMap<>();
        processDataFeed(newStops);
        this.allStops = newStops;
        long currentRunCount = runCount.incrementAndGet();
        log.info("Updated stops. New count: {}, Run count: {}\n", newStops.size(), currentRunCount);
    }

    public void processDataFeed(Map<String, Stop> newStops) {
        log.info("Processing arrivals...");
        long startTime = System.currentTimeMillis();

        // Transiter API paginates results in batches of 100
        String nextId = null;
        do {
            String url = transiterUrl + "/systems/us-ny-subway/stops?skip_service_maps=true&skip_alerts=true&skip_transfers=true";
            if (nextId != null) {
                url += "&first_id=" + nextId;
            }

            try {
                ResponseEntity<TransiterResponse> response = restTemplate.getForEntity(url, TransiterResponse.class);
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    TransiterResponse body = response.getBody();

                    processStops(body.stops(), newStops);

                    nextId = body.nextId();
                } else {
                    log.warn("Received non-OK status code or null body: {}", response.getStatusCode());
                    break;
                }
            } catch (Exception e) {
                log.error("Error fetching stops: {}", e.getMessage(), e);
                break;
            }
        } while (nextId != null);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info("Run #{}, Execution time: {} seconds {} milliseconds", runCount, duration / 1000, duration % 1000);
    }

    private void processStops(List<Stop> fetchedStops, Map<String, Stop> newStops) {
        // all Arrival times are calculated against same time for consistency
        long currentTime = System.currentTimeMillis() / 1000;

        // a Station is the parent Stop of two Platform Stops that represent northbound and southbound trips
        for (Stop fetchedStop : fetchedStops) {
            if (fetchedStop.type().equals("STATION")) {
                processStation(fetchedStop, newStops, currentTime);
            }
        }
    }

    /* I'm not sure what the most performant solution is to determine the earliest arrival time for each platform.
    It seems like the data feed is output in chronological order, but I don't feel like that's a safe assumption
    to make. For the moment, I figure it would be more robust to iterate over everything to ensure that we're
    actually getting the earliest arrival time. */
    private void processStation(Stop station, Map<String, Stop> newStops, long currentTime) {
        station.stopTimes().ifPresent(stopTimes -> {
            for (StopTime stopTime : stopTimes) {
                String tripId = stopTime.trip().id();
                String platformId = stopTime.stop().id();
                String lineId = stopTime.trip().route().id();
                String color = stopTime.trip().route().color();
                String destination = stopTime.destination().name();
                long arrivalTime = stopTime.arrival().time();

                Map<String, Arrival> platformArrivals = persistentArrivals
                        .computeIfAbsent(platformId, k -> new HashMap<>());

                Arrival existingArrival = platformArrivals.get(lineId);

                log.debug("Current time: {}, Arrival time: {}, Difference: {} seconds",
                        currentTime, arrivalTime, (arrivalTime - currentTime));

                if (arrivalTime != 0 && arrivalTime > currentTime) {
                    double waitTime = calculateWaitTime(arrivalTime, currentTime);

                    if (existingArrival == null || existingArrival.getCurrentArrivalTime() < currentTime) {
                        // new arrival for this line or existing arrival is in the past
                        Arrival newArrival = new Arrival(tripId, lineId, color, destination, arrivalTime);
                        newArrival.setMovingAverage(waitTime);
                        platformArrivals.put(lineId, newArrival);
                        log.info("New/Updated arrival for line: {}, tripId: {}, arrivalTime: {}", lineId, tripId, arrivalTime);
                    } else if (arrivalTime < existingArrival.getCurrentArrivalTime()) {
                        // if an earlier arrival time is found, regardless of tripId, update the arrival time
                        existingArrival.setTripId(tripId);
                        existingArrival.setPreviousArrivalTime(existingArrival.getCurrentArrivalTime());
                        existingArrival.setCurrentArrivalTime(arrivalTime);

                        double oldAverage = existingArrival.getMovingAverage();
                        double newAverage = calculateMovingAverage(oldAverage, waitTime);

                        // TODO
                        // I think this needs refactored because this gets called every time an earlier arrival time
                        // for a particular line, which might artificially increase the moving average wait time.
                        existingArrival.setMovingAverage(newAverage);
                    } else if (existingArrival.getTripId().equals(tripId)) {
                        // if no earlier trips were found, update the current trip to its latest expected arrival time
                        existingArrival.setPreviousArrivalTime(existingArrival.getCurrentArrivalTime());
                        existingArrival.setCurrentArrivalTime(arrivalTime);

                        double oldAverage = existingArrival.getMovingAverage();
                        double newAverage = calculateMovingAverage(oldAverage, waitTime);
                        existingArrival.setMovingAverage(newAverage);
                    }
                }
            }
        });

        // TODO
        // after the earliest next arrival time is found, iterate through the arrival times and setMovingAverage() here?
        List<Stop.ChildStop> processedPlatforms = station.childStops().stream()
                .map(childStop -> {
                    Map<String, Arrival> lineArrivals = persistentArrivals.getOrDefault(childStop.id(), new HashMap<>());
                    return new Stop.ChildStop(childStop.id(), childStop.name(), lineArrivals);
                })
                .collect(Collectors.toList());

        Stop processedStation = new Stop(
                station.id(),
                station.name(),
                station.latitude(),
                station.longitude(),
                station.type(),
                processedPlatforms
        );

        newStops.put(station.id(), processedStation);
    }

    private double calculateWaitTime(long arrivalTime, long currentTime) {
        return Math.max(0, (arrivalTime - currentTime) / 60.0);
    }

    public double calculateMovingAverage(double oldAverage, double newValue) {
//        Adaptive Moving Average formula
//        double volatilityFactor = 0.1;
//        double volatility = Math.abs(newValue - oldAverage);
//        double dynamicAlpha = ALPHA / (1 + volatilityFactor * volatility);
//        dynamicAlpha = Math.max(0.01, Math.min(dynamicAlpha, 1.0));
//        return dynamicAlpha * newValue + (1 - dynamicAlpha) * oldAverage;

        // Exponential Moving Average formula
        return ALPHA * newValue + (1 - ALPHA) * oldAverage;
    }

    public long getRunCount() {
        return runCount.get();
    }
}