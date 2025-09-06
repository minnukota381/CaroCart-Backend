package com.carocart.orderservice.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.math.BigDecimal;
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
    @Column(name = "coupon_code")
    private String couponCode;

    @Column(name = "coupon_applied")
    private String couponApplied;
    
    @Column(name = "discount_amount")
    private Double discountAmount = 0.0;

    // OPTIONAL but useful so net = total - commission - penalty + adj
    @Column(name = "vendor_penalty_amount", precision=12, scale=2, nullable = false)
    private BigDecimal vendorPenaltyAmount = BigDecimal.ZERO;

    @Column(name = "vendor_adjustment_amount", precision=12, scale=2, nullable = false)
    private BigDecimal vendorAdjustmentAmount = BigDecimal.ZERO;

    // Delivery completion moment used for payout windows
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    // Settlement state for vendor’s payout
    @Enumerated(EnumType.STRING)
    @Column(name = "vendor_settlement_status", length = 10, nullable = false)
    private VendorSettlementStatus vendorSettlementStatus = VendorSettlementStatus.UNPAID;

    // Set after payout
    @Column(name = "vendor_payout_id", length = 64)
    private String vendorPayoutId;
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
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }
    
    public String getCouponApplied() { return couponApplied; }
    public void setCouponApplied(String couponApplied) { this.couponApplied = couponApplied; }
    
	public String getVendorPayoutId() {
		return vendorPayoutId;
	}
	public void setVendorPayoutId(String vendorPayoutId) {
		this.vendorPayoutId = vendorPayoutId;
	}
	public LocalDateTime getDeliveredAt() {
		return deliveredAt;
	}
	public void setDeliveredAt(LocalDateTime deliveredAt) {
		this.deliveredAt = deliveredAt;
	}

	public BigDecimal getVendorPenaltyAmount() {
		return vendorPenaltyAmount;
	}
	public void setVendorPenaltyAmount(BigDecimal vendorPenaltyAmount) {
		this.vendorPenaltyAmount = vendorPenaltyAmount;
	}
	public BigDecimal getVendorAdjustmentAmount() {
		return vendorAdjustmentAmount;
	}
	public void setVendorAdjustmentAmount(BigDecimal vendorAdjustmentAmount) {
		this.vendorAdjustmentAmount = vendorAdjustmentAmount;
	}
	public VendorSettlementStatus getVendorSettlementStatus() {
		return vendorSettlementStatus;
	}
	public void setVendorSettlementStatus(VendorSettlementStatus vendorSettlementStatus) {
		this.vendorSettlementStatus = vendorSettlementStatus;
	}
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
