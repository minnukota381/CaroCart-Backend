package com.carocart.paymentservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Payment {

	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;
	 private Long orderId;
	 private Long userId;
	 private Double amount;
	 private String paymentMethod;
	 private String razorpayOrderId;
	 private String razorpayPaymentId;
	 private String razorpaySignature;
	 private String status;
	 private LocalDateTime transactionDate;
	 
	 public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
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
		public String getPaymentMethod() {
			return paymentMethod;
		}
		public void setPaymentMethod(String paymentMethod) {
			this.paymentMethod = paymentMethod;
		}
		public Double getAmount() {
			return amount;
		}
		public void setAmount(Double amount) {
			this.amount = amount;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public LocalDateTime getTransactionDate() {
			return transactionDate;
		}
		public void setTransactionDate(LocalDateTime transactionDate) {
			this.transactionDate = transactionDate;
		}


	    
}