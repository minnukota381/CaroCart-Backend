package com.carocart.delivery.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_payouts")
public class DeliveryPayout {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false) private Long deliveryBoyId;
    @Column(nullable=false) private Double totalAmount;
    @Column(nullable=false) private Integer items; // #earnings included
    @Column(nullable=false) private LocalDateTime createdAt = LocalDateTime.now();

    // audit / optional
    private String notes;             // UTR/TxnId/remarks
    private Long markedByAdminId;     // if you can resolve admin id
    private LocalDateTime windowFrom; // last paid at (exclusive)
    private LocalDateTime windowTo;   // now

    // getters/setters
    public Long getId() { return id; }
    public Long getDeliveryBoyId() { return deliveryBoyId; }
    public void setDeliveryBoyId(Long deliveryBoyId) { this.deliveryBoyId = deliveryBoyId; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public Integer getItems() { return items; }
    public void setItems(Integer items) { this.items = items; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Long getMarkedByAdminId() { return markedByAdminId; }
    public void setMarkedByAdminId(Long markedByAdminId) { this.markedByAdminId = markedByAdminId; }
    public LocalDateTime getWindowFrom() { return windowFrom; }
    public void setWindowFrom(LocalDateTime windowFrom) { this.windowFrom = windowFrom; }
    public LocalDateTime getWindowTo() { return windowTo; }
    public void setWindowTo(LocalDateTime windowTo) { this.windowTo = windowTo; }
}
