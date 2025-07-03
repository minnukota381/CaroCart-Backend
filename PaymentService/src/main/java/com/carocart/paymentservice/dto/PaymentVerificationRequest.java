package com.carocart.paymentservice.dto;

public class PaymentVerificationRequest {
    private Long orderId;  // Use for legacy flow (optional)
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    // Getters and Setters
	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	public String getRazorpayOrderId() {
		return razorpayOrderId;
	}
	public void setRazorpayOrderId(String razorpayOrderId) {
		this.razorpayOrderId = razorpayOrderId;
	}
	public String getRazorpayPaymentId() {
		return razorpayPaymentId;
	}
	public void setRazorpayPaymentId(String razorpayPaymentId) {
		this.razorpayPaymentId = razorpayPaymentId;
	}
	public String getRazorpaySignature() {
		return razorpaySignature;
	}
	public void setRazorpaySignature(String razorpaySignature) {
		this.razorpaySignature = razorpaySignature;
	}
    
}
