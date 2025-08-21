// DeliveryFeeResponse.java
package com.carocart.delivery.dto;

public class DeliveryFeeResponse {
    private double distanceKm;
    private double fee;
    private boolean allowed;

    public DeliveryFeeResponse() {}
    public DeliveryFeeResponse(double distanceKm, double fee, boolean allowed) {
        this.distanceKm = distanceKm;
        this.fee = fee;
        this.allowed = allowed;
    }
	public double getDistanceKm() {
		return distanceKm;
	}
	public void setDistanceKm(double distanceKm) {
		this.distanceKm = distanceKm;
	}
	public double getFee() {
		return fee;
	}
	public void setFee(double fee) {
		this.fee = fee;
	}
	public boolean isAllowed() {
		return allowed;
	}
	public void setAllowed(boolean allowed) {
		this.allowed = allowed;
	}

    // getters/setters
}
