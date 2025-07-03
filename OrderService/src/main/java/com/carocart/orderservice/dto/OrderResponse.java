package com.carocart.orderservice.dto;

import java.time.LocalDateTime;
import java.util.List;

// DTO for API responses sent to frontend after order placement, fetching orders, etc.
public class OrderResponse {

    private Long orderId;
    private Long userId;
    private String shippingAddress;
    private Long addressId;
    private Double totalAmount;
    private String status;         // e.g. PLACED, SHIPPED, DELIVERED, CANCELLED
    private String paymentStatus;  // e.g. PAID, PENDING, COD, REFUNDED
    private String paymentId;      // Razorpay/cash/upi/payment txn id
    private LocalDateTime orderDate;
    private LocalDateTime updatedAt;
    private List<OrderItemDTO> items;

    // --- Constructors ---
    public OrderResponse() {}

    public OrderResponse(Long orderId, Long userId, String shippingAddress, Long addressId, Double totalAmount,
                         String status, String paymentStatus, String paymentId,
                         LocalDateTime orderDate, LocalDateTime updatedAt,
                         List<OrderItemDTO> items) {
        this.orderId = orderId;
        this.userId = userId;
        this.shippingAddress = shippingAddress;
        this.addressId = addressId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.paymentId = paymentId;
        this.orderDate = orderDate;
        this.updatedAt = updatedAt;
        this.items = items;
    }

    // Entity-to-DTO constructor (if needed)
    public OrderResponse(com.carocart.orderservice.entity.Order order) {
        this.orderId = order.getId();
        this.userId = order.getUserId();
        this.shippingAddress = order.getShippingAddress();
        // If addressId exists as a field in your entity, map here. Else leave null.
        try {
            this.addressId = (Long) order.getClass().getDeclaredMethod("getAddressId").invoke(order);
        } catch (Exception ignored) {}
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();
        this.paymentStatus = order.getPaymentStatus();
        this.paymentId = order.getPaymentId();
        this.orderDate = order.getOrderDate();
        this.updatedAt = order.getUpdatedAt();
        if (order.getOrderItems() != null) {
            this.items = order.getOrderItems().stream()
                .map(OrderItemDTO::new)
                .toList();
        }
    }

    // --- Getters & Setters ---
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
}
