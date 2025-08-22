package com.carocart.delivery.dto;

import java.time.LocalDateTime;

public class DeliveryEarningCreateRequest {
    private Long orderItemId;
    private Long deliveryBoyId;
    private Double fee;
    private Double tip;
    private Double penalty;
    private LocalDateTime deliveredAt;

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
