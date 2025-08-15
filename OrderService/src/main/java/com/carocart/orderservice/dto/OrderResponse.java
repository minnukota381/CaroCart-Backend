package com.carocart.orderservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    private Long orderId;
    private Long userId;
    private String shippingAddress;
    private Long addressId;
    private Double totalAmount;
    private String status;
    private String paymentStatus;
    private String paymentId;
    private LocalDateTime orderDate;
    private LocalDateTime updatedAt;
    private List<OrderItemDTO> items;
    private Long deliveryBoyId;
    private String deliveryStatus;
    private Long vendorId;

    // ✅ NEW: address-based location (from AddressService)
    private Double addressLat;
    private Double addressLng;

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

    public OrderResponse(com.carocart.orderservice.entity.Order order) {
        this.orderId = order.getId();
        this.userId = order.getUserId();
        this.shippingAddress = order.getShippingAddress();
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();
        this.paymentStatus = order.getPaymentStatus();
        this.paymentId = order.getPaymentId();
        this.orderDate = order.getOrderDate();
        this.updatedAt = order.getUpdatedAt();
        this.deliveryBoyId = order.getDeliveryBoyId();
        this.deliveryStatus = order.getDeliveryStatus();
        this.vendorId = order.getVendorId();

        try {
            this.addressId = (Long) order.getClass().getDeclaredMethod("getAddressId").invoke(order);
        } catch (Exception ignored) {}

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

    public Long getDeliveryBoyId() { return deliveryBoyId; }
    public void setDeliveryBoyId(Long deliveryBoyId) { this.deliveryBoyId = deliveryBoyId; }

    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public Double getAddressLat() { return addressLat; }
    public void setAddressLat(Double addressLat) { this.addressLat = addressLat; }

    public Double getAddressLng() { return addressLng; }
    public void setAddressLng(Double addressLng) { this.addressLng = addressLng; }
}
