package com.transit.backend.models;

public class Arrival {
    private String tripId;
    private final String lineId;
    private final String color;
    private final String destination;
    private long currentArrivalTime;
    private long previousArrivalTime;
    private double movingAverage;

    public Arrival(String tripId, String lineId, String color, String destination, long currentArrivalTime) {
        this.tripId = tripId;
        this.lineId = lineId;
        this.color = color;
        this.destination = destination;
        this.currentArrivalTime = currentArrivalTime;
        this.previousArrivalTime = 0;
        this.movingAverage = 0.0;
    }

    public String getTripId() {
        return tripId;
    }

    public String getLineId() {
        return lineId;
    }

    public String getColor() {
        return color;
    }

    public String getDestination() {
        return destination;
    }

    public long getCurrentArrivalTime() {
        return currentArrivalTime;
    }

    public long getPreviousArrivalTime() {
        return previousArrivalTime;
    }

    public double getMovingAverage() {
        return movingAverage;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public void setCurrentArrivalTime(long newTime) {
        this.currentArrivalTime = newTime;
    }

    public void setPreviousArrivalTime(long newTime) {
        this.previousArrivalTime = newTime;
    }

    public void setMovingAverage(double movingAverage) {
        this.movingAverage = movingAverage;
    }

}