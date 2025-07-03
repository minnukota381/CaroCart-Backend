package com.carocart.delivery.dto;

public class ClaimOrderDTO {
    private Long orderId;

    public ClaimOrderDTO() {}

    public ClaimOrderDTO(Long orderId) {
        this.orderId = orderId;
    }

    // Getter and setter
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
}
