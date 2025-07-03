package com.carocart.paymentservice.dto;

import java.time.LocalDateTime;

/**
 * DTO for sending payment info to frontend.
 * Supports Razorpay, manual UPI, and status responses.
 */
public class PaymentResponseDTO {
    // Generic payment info/status
    private String paymentId;
    private Long orderId;
    private String upiTransactionId;
    private String status;
    private LocalDateTime updatedAt;

    // Razorpay order creation
    private String id; // Razorpay order ID
    private String currency;
    private Integer amount;
    private String key;

    public PaymentResponseDTO() {}

    // Constructor for Razorpay order creation response
    public PaymentResponseDTO(String id, Integer amount, String currency, String key) {
        this.id = id;
        this.amount = amount;
        this.currency = currency;
        this.key = key;
    }

    // Constructor for generic payment status/UPI/manual flows
    public PaymentResponseDTO(String paymentId, Long orderId, String upiTransactionId, String status, LocalDateTime updatedAt) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.upiTransactionId = upiTransactionId;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    // Optionally: Add a constructor for UPI collect, if you want
    public PaymentResponseDTO(String paymentId, Long orderId, String upiTransactionId, String status) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.upiTransactionId = upiTransactionId;
        this.status = status;
    }

    // Getters and setters

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getUpiTransactionId() {
        return upiTransactionId;
    }

    public void setUpiTransactionId(String upiTransactionId) {
        this.upiTransactionId = upiTransactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "PaymentResponseDTO{" +
                "paymentId='" + paymentId + '\'' +
                ", orderId=" + orderId +
                ", upiTransactionId='" + upiTransactionId + '\'' +
                ", status='" + status + '\'' +
                ", updatedAt=" + updatedAt +
                ", id='" + id + '\'' +
                ", currency='" + currency + '\'' +
                ", amount=" + amount +
                ", key='" + key + '\'' +
                '}';
    }
}
