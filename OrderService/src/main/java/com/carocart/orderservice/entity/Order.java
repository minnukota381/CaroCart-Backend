package com.carocart.orderservice.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; 
    private LocalDateTime orderDate;
    private String status; // e.g. PLACED, PROCESSING, SHIPPED, DELIVERED, CANCELLED

    private Double totalAmount;   // items total
    private Double deliveryFee;   // ✅ new
    private Double finalAmount;   // ✅ new (totalAmount + deliveryFee - discounts)

    @Column(length = 1000)
    private String shippingAddress;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "address_id")
    private Long addressId;

    @Column(name = "platform_commission_pct")
    private Double platformCommissionPct;

    @Column(name = "platform_commission_amount")
    private Double platformCommissionAmount;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "delivery_boy_id")
    private Long deliveryBoyId;

    @Column(name = "delivery_status")
    private String deliveryStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<OrderItem> orderItems;

    private LocalDateTime updatedAt;

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public Double getDeliveryFee() { return deliveryFee; } // ✅ new
    public void setDeliveryFee(Double deliveryFee) { this.deliveryFee = deliveryFee; }

    public Double getFinalAmount() { return finalAmount; } // ✅ new
    public void setFinalAmount(Double finalAmount) { this.finalAmount = finalAmount; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }

    public Double getPlatformCommissionPct() { return platformCommissionPct; }
    public void setPlatformCommissionPct(Double platformCommissionPct) { this.platformCommissionPct = platformCommissionPct; }

    public Double getPlatformCommissionAmount() { return platformCommissionAmount; }
    public void setPlatformCommissionAmount(Double platformCommissionAmount) { this.platformCommissionAmount = platformCommissionAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public Long getDeliveryBoyId() { return deliveryBoyId; }
    public void setDeliveryBoyId(Long deliveryBoyId) { this.deliveryBoyId = deliveryBoyId; }

    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // --- JPA Lifecycle ---
    @PrePersist
    public void onCreate() {
        if (this.orderDate == null) {
            this.orderDate = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
