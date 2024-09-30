package com.transit.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Maps the "StopTime" entity nested under each "Stop" from the Transiter API's "Stop" feed.
// Half of the fields are ignored via the @JsonIgnoreProperties annotation.
@JsonIgnoreProperties(ignoreUnknown = true)
public record StopTime(
        Trip trip,
        Stop.ChildStop stop,
        Stop.ChildStop destination,
        ArrivalTime arrival
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Trip(
            String id,
            Route route
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Route(
                String id,
                String color
        ) {}
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ArrivalTime(
            long time
    ) {}
}