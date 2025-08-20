// DeliveryFeeResponse.java
package com.carocart.orderservice.dto;

public class DeliveryFeeResponse {
    private boolean allowed;
    private Double fee;
    private String message;
	public boolean isAllowed() {
		return allowed;
	}
	public void setAllowed(boolean allowed) {
		this.allowed = allowed;
	}
	public Double getFee() {
		return fee;
	}
	public void setFee(Double fee) {
		this.fee = fee;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

    // getters/setters
}
