package com.transit.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransiterResponse(
        List<Stop> stops,
        String nextId
) {}