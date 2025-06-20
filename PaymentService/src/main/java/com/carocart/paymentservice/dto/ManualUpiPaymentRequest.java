package com.carocart.paymentservice.dto;

public class ManualUpiPaymentRequest {
    private Long orderId;
    private Long userId;
    private Double amount;
    private String upiTransactionId;
	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public String getUpiTransactionId() {
		return upiTransactionId;
	}
	public void setUpiTransactionId(String upiTransactionId) {
		this.upiTransactionId = upiTransactionId;
	}
    
}
