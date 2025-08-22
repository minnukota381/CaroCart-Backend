package com.carocart.delivery.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_earnings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"orderItemId"})
})
public class DeliveryEarning {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false) private Long deliveryBoyId;
    @Column(nullable=false) private Long orderItemId;

    @Column(nullable=false) private Double fee;      // base earning
    @Column(nullable=false) private Double tip = 0.0;
    @Column(nullable=false) private Double penalty = 0.0;

    @Column(nullable=false) private Double netAmount; // fee + tip - penalty
    @Column(nullable=false) private String status = "PENDING"; // PENDING|PAID

    @Column(nullable=false) private LocalDateTime deliveredAt;
    @Column(nullable=false) private LocalDateTime createdAt = LocalDateTime.now();

    // getters/setters
    public Long getId() { return id; }
    public Long getDeliveryBoyId() { return deliveryBoyId; }
    public void setDeliveryBoyId(Long deliveryBoyId) { this.deliveryBoyId = deliveryBoyId; }
    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }
    public Double getFee() { return fee; }
    public void setFee(Double fee) { this.fee = fee; }
    public Double getTip() { return tip; }
    public void setTip(Double tip) { this.tip = tip; }
    public Double getPenalty() { return penalty; }
    public void setPenalty(Double penalty) { this.penalty = penalty; }
    public Double getNetAmount() { return netAmount; }
    public void setNetAmount(Double netAmount) { this.netAmount = netAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
