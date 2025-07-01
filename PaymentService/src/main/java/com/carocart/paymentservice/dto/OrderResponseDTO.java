package com.carocart.paymentservice.dto;

public class OrderResponseDTO {
    private Long orderId;
    private String status;
    private String message;
	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

    // Add more fields if your system returns more data
    // Getters, setters, constructors
    
}
