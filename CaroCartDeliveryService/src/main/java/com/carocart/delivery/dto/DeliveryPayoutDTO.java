package com.carocart.delivery.dto;

import java.time.LocalDateTime;

public class DeliveryPayoutDTO {
    private Long payoutId;
    private Long deliveryBoyId;
    private double totalAmount;
    private int items;
    private LocalDateTime createdAt;
    private String notes;

    public DeliveryPayoutDTO() {}
    public DeliveryPayoutDTO(Long payoutId, Long deliveryBoyId, double totalAmount, int items,
                             LocalDateTime createdAt, String notes) {
        this.payoutId = payoutId; this.deliveryBoyId = deliveryBoyId; this.totalAmount = totalAmount;
        this.items = items; this.createdAt = createdAt; this.notes = notes;
    }
    public Long getPayoutId() { return payoutId; }
    public void setPayoutId(Long payoutId) { this.payoutId = payoutId; }
    public Long getDeliveryBoyId() { return deliveryBoyId; }
    public void setDeliveryBoyId(Long deliveryBoyId) { this.deliveryBoyId = deliveryBoyId; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public int getItems() { return items; }
    public void setItems(int items) { this.items = items; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
