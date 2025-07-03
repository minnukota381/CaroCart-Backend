package com.carocart.delivery.dto;

public class DeliveryStatusUpdateDTO {
    private Long orderId;
    private String status;

    public DeliveryStatusUpdateDTO() {}

    public DeliveryStatusUpdateDTO(Long orderId, String status) {
        this.orderId = orderId;
        this.status = status;
    }

    // Getters and setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
