package com.carocart.orderservice.dto;

import java.time.LocalDateTime;

public class DeliveryEarningCreateRequest {
    private Long orderItemId;
    private Long deliveryBoyId;
    private Double fee;
    private Double tip;
    private Double penalty;
    private LocalDateTime deliveredAt;

    public DeliveryEarningCreateRequest() {}
    public DeliveryEarningCreateRequest(Long orderItemId, Long deliveryBoyId, Double fee, Double tip, Double penalty, LocalDateTime deliveredAt) {
        this.orderItemId = orderItemId; this.deliveryBoyId = deliveryBoyId; this.fee = fee;
        this.tip = tip; this.penalty = penalty; this.deliveredAt = deliveredAt;
    }
    // getters/setters
    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }
    public Long getDeliveryBoyId() { return deliveryBoyId; }
    public void setDeliveryBoyId(Long deliveryBoyId) { this.deliveryBoyId = deliveryBoyId; }
    public Double getFee() { return fee; }
    public void setFee(Double fee) { this.fee = fee; }
    public Double getTip() { return tip; }
    public void setTip(Double tip) { this.tip = tip; }
    public Double getPenalty() { return penalty; }
    public void setPenalty(Double penalty) { this.penalty = penalty; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
}
