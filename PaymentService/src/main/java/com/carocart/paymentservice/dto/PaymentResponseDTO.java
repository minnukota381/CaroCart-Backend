package com.carocart.paymentservice.dto;

import java.time.LocalDateTime;

public class PaymentResponseDTO {

    private String paymentId;
    private String status;
    private LocalDateTime timestamp;

    public PaymentResponseDTO() {
    }

    public PaymentResponseDTO(String paymentId, String status, LocalDateTime timestamp) {
        this.paymentId = paymentId;
        this.status = status;
        this.timestamp = timestamp;
    }

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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "PaymentResponseDTO{" +
                "paymentId='" + paymentId + '\'' +
                ", status='" + status + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
