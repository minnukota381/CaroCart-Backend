package com.carocart.delivery.dto;

public class DeliveryStatusUpdateDTO {
    private Long orderId;
    private String status;
    private String otp;

    public DeliveryStatusUpdateDTO() {}

    public DeliveryStatusUpdateDTO(Long orderId, String status) {
        this.orderId = orderId;
        this.status = status;
    }

    // Getters and setters for all fields, including otp
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    
}
