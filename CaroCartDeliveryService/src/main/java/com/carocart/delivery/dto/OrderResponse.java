package com.carocart.delivery.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
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

    // Items (optional – used by detail screens, not needed for the list card)
    private List<OrderItemDTO> items;

    // Delivery (order-level)
    private Long deliveryBoyId;
    private String deliveryStatus;
    private Long vendorId;

    // Pricing
    private Double deliveryFee;
    private Double finalAmount;

    // Coordinates for tracking / map
    private Double addressLat;
    private Double addressLng;

    // Enrichment from OrderService (these must be set there)
    private String userName;     // "First Last"
    private String userPhone;    // customer mobile
    private Double vendorLat;    // vendor coords
    private Double vendorLng;
    private String vendorPhone;  // vendor contact

    // Optional compact product summary for the card
    private String productName;  // first item name or summary
    private Integer quantity;    // total quantity across items

    public OrderResponse() {
    }

    // ===== Getters & Setters =====

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

    public Double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(Double deliveryFee) { this.deliveryFee = deliveryFee; }

    public Double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(Double finalAmount) { this.finalAmount = finalAmount; }

    public Double getAddressLat() { return addressLat; }
    public void setAddressLat(Double addressLat) { this.addressLat = addressLat; }

    public Double getAddressLng() { return addressLng; }
    public void setAddressLng(Double addressLng) { this.addressLng = addressLng; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public Double getVendorLat() { return vendorLat; }
    public void setVendorLat(Double vendorLat) { this.vendorLat = vendorLat; }

    public Double getVendorLng() { return vendorLng; }
    public void setVendorLng(Double vendorLng) { this.vendorLng = vendorLng; }

    public String getVendorPhone() { return vendorPhone; }
    public void setVendorPhone(String vendorPhone) { this.vendorPhone = vendorPhone; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
