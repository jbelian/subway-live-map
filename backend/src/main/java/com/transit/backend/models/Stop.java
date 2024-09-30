package com.transit.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;
import java.util.Optional;

// Maps the "Stop" entity from the Transiter API's "Stop" feed.
// Half of the fields are ignored via the @JsonIgnoreProperties annotation.

// Confusingly, each Stop has a list of StopTimes, and each StopTime includes a Stop "stop" and a Stop "destination".
// Also, each Stop has three related entities from the data feed: for example, the IDs 101, 101N, and 101S.
// 101 references two child stops, 101N and 101S, and includes all of their trips. It has the type "STATION".
// 101N and 101S represent northbound and southbound trips. They have the type "PLATFORM".
@JsonIgnoreProperties(ignoreUnknown = true)
public record Stop(
        String id,
        String name,
        double latitude,
        double longitude,
        String type,
        List<ChildStop> childStops,
        Optional<List<StopTime>> stopTimes
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChildStop(
            String id,
            String name,
            Map<String, Arrival> lineArrivals
    ) {}

    public Stop(String id, String name, double latitude, double longitude, String type, List<ChildStop> childStops) {
        this(id, name, latitude, longitude, type, childStops, Optional.empty());
    }
}
