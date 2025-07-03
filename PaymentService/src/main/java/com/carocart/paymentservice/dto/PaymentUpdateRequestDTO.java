package com.carocart.paymentservice.dto;

public class PaymentUpdateRequestDTO {
    private String paymentId;
    private String status;
    private String comment;
    // Getters and Setters
	public String getPaymentId() {
		return paymentId;
	}
	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
    
}
