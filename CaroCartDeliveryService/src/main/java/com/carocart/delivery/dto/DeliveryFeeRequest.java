// DeliveryFeeRequest.java
package com.carocart.delivery.dto;

public class DeliveryFeeRequest {
    private double vendorLat;
    private double vendorLng;
    private double userLat;
    private double userLng;
    private String vendorType; // FOOD or GROCERY
	public double getVendorLat() {
		return vendorLat;
	}
	public void setVendorLat(double vendorLat) {
		this.vendorLat = vendorLat;
	}
	public double getVendorLng() {
		return vendorLng;
	}
	public void setVendorLng(double vendorLng) {
		this.vendorLng = vendorLng;
	}
	public double getUserLat() {
		return userLat;
	}
	public void setUserLat(double userLat) {
		this.userLat = userLat;
	}
	public double getUserLng() {
		return userLng;
	}
	public void setUserLng(double userLng) {
		this.userLng = userLng;
	}
	public String getVendorType() {
		return vendorType;
	}
	public void setVendorType(String vendorType) {
		this.vendorType = vendorType;
	}

    // getters/setters
}
