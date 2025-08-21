// DeliveryFeeRequest.java
package com.carocart.orderservice.dto;

public class DeliveryFeeRequest {
    private Double vendorLat;
    private Double vendorLng;
    private Double userLat;
    private Double userLng;
    private boolean grocery;
    private Double cartTotal;
	public Double getVendorLat() {
		return vendorLat;
	}
	public void setVendorLat(Double vendorLat) {
		this.vendorLat = vendorLat;
	}
	public Double getVendorLng() {
		return vendorLng;
	}
	public void setVendorLng(Double vendorLng) {
		this.vendorLng = vendorLng;
	}
	public Double getUserLat() {
		return userLat;
	}
	public void setUserLat(Double userLat) {
		this.userLat = userLat;
	}
	public Double getUserLng() {
		return userLng;
	}
	public void setUserLng(Double userLng) {
		this.userLng = userLng;
	}
	public boolean isGrocery() {
		return grocery;
	}
	public void setGrocery(boolean grocery) {
		this.grocery = grocery;
	}
	public Double getCartTotal() {
		return cartTotal;
	}
	public void setCartTotal(Double cartTotal) {
		this.cartTotal = cartTotal;
	}

    // getters/setters
}
