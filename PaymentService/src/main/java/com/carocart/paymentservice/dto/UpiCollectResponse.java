package com.carocart.paymentservice.dto;

public class UpiCollectResponse {
    private String requestId;
    private String paymentLink;
    private String expiry;
    // Getters and Setters
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String getPaymentLink() {
		return paymentLink;
	}
	public void setPaymentLink(String paymentLink) {
		this.paymentLink = paymentLink;
	}
	public String getExpiry() {
		return expiry;
	}
	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}
    
}
