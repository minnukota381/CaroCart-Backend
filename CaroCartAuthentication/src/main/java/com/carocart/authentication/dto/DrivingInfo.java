package com.carocart.authentication.dto;

public class DrivingInfo {
    private final double km;
    private final int minutes;

    public DrivingInfo(double km, int minutes) {
        this.km = km;
        this.minutes = minutes;
    }

    public double getKm() { return km; }
    public int getMinutes() { return minutes; }
    
    
}
