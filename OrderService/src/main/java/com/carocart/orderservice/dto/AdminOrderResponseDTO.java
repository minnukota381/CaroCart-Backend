package com.carocart.orderservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AdminOrderResponseDTO {

    private Long orderId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // User info
    private Long userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String userAddress;
    private Double userLatitude;
    private Double userLongitude;

    // Vendor info
    private Long vendorId;
    private String vendorName;
    private String vendorEmail;
    private String vendorPhone;
    private String vendorAddress;
    private Double vendorLatitude;
    private Double vendorLongitude;

    // DeliveryBoy info
    private Long deliveryBoyId;
    private String deliveryBoyName;
    private String deliveryBoyPhone;
    private Double deliveryBoyLatitude;
    private Double deliveryBoyLongitude;

    // Order Items
    private List<OrderItemDTO> orderItems;

    public AdminOrderResponseDTO() {
    }

    public AdminOrderResponseDTO(Long orderId, String status, LocalDateTime createdAt, LocalDateTime updatedAt,
                                 Long userId, String userName, String userEmail, String userPhone, String userAddress,
                                 Double userLatitude, Double userLongitude,
                                 Long vendorId, String vendorName, String vendorEmail, String vendorPhone, String vendorAddress,
                                 Double vendorLatitude, Double vendorLongitude,
                                 Long deliveryBoyId, String deliveryBoyName, String deliveryBoyPhone,
                                 Double deliveryBoyLatitude, Double deliveryBoyLongitude,
                                 List<OrderItemDTO> orderItems) {
        this.orderId = orderId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
        this.userAddress = userAddress;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.vendorEmail = vendorEmail;
        this.vendorPhone = vendorPhone;
        this.vendorAddress = vendorAddress;
        this.vendorLatitude = vendorLatitude;
        this.vendorLongitude = vendorLongitude;
        this.deliveryBoyId = deliveryBoyId;
        this.deliveryBoyName = deliveryBoyName;
        this.deliveryBoyPhone = deliveryBoyPhone;
        this.deliveryBoyLatitude = deliveryBoyLatitude;
        this.deliveryBoyLongitude = deliveryBoyLongitude;
        this.orderItems = orderItems;
    }

    // --- Getters & Setters ---
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getUserAddress() { return userAddress; }
    public void setUserAddress(String userAddress) { this.userAddress = userAddress; }

    public Double getUserLatitude() { return userLatitude; }
    public void setUserLatitude(Double userLatitude) { this.userLatitude = userLatitude; }

    public Double getUserLongitude() { return userLongitude; }
    public void setUserLongitude(Double userLongitude) { this.userLongitude = userLongitude; }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public String getVendorEmail() { return vendorEmail; }
    public void setVendorEmail(String vendorEmail) { this.vendorEmail = vendorEmail; }

    public String getVendorPhone() { return vendorPhone; }
    public void setVendorPhone(String vendorPhone) { this.vendorPhone = vendorPhone; }

    public String getVendorAddress() { return vendorAddress; }
    public void setVendorAddress(String vendorAddress) { this.vendorAddress = vendorAddress; }

    public Double getVendorLatitude() { return vendorLatitude; }
    public void setVendorLatitude(Double vendorLatitude) { this.vendorLatitude = vendorLatitude; }

    public Double getVendorLongitude() { return vendorLongitude; }
    public void setVendorLongitude(Double vendorLongitude) { this.vendorLongitude = vendorLongitude; }

    public Long getDeliveryBoyId() { return deliveryBoyId; }
    public void setDeliveryBoyId(Long deliveryBoyId) { this.deliveryBoyId = deliveryBoyId; }

    public String getDeliveryBoyName() { return deliveryBoyName; }
    public void setDeliveryBoyName(String deliveryBoyName) { this.deliveryBoyName = deliveryBoyName; }

    public String getDeliveryBoyPhone() { return deliveryBoyPhone; }
    public void setDeliveryBoyPhone(String deliveryBoyPhone) { this.deliveryBoyPhone = deliveryBoyPhone; }

    public Double getDeliveryBoyLatitude() { return deliveryBoyLatitude; }
    public void setDeliveryBoyLatitude(Double deliveryBoyLatitude) { this.deliveryBoyLatitude = deliveryBoyLatitude; }

    public Double getDeliveryBoyLongitude() { return deliveryBoyLongitude; }
    public void setDeliveryBoyLongitude(Double deliveryBoyLongitude) { this.deliveryBoyLongitude = deliveryBoyLongitude; }

    public List<OrderItemDTO> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemDTO> orderItems) { this.orderItems = orderItems; }
}
