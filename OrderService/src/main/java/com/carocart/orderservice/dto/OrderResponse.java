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

    // NEW FIELDS
    private String deliveryStatus;
    private Long deliveryBoyId;
    private String userContact;  // Add user's phone/email here

    public OrderResponse() {}

    public OrderResponse(com.carocart.orderservice.entity.Order order) {
        this.orderId = order.getId();
        this.userId = order.getUserId();
        this.shippingAddress = order.getShippingAddress();
        try {
            this.addressId = (Long) order.getClass().getDeclaredMethod("getAddressId").invoke(order);
        } catch (Exception ignored) {}

        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();
        this.paymentStatus = order.getPaymentStatus();
        this.paymentId = order.getPaymentId();
        this.orderDate = order.getOrderDate();
        this.updatedAt = order.getUpdatedAt();

        this.deliveryStatus = order.getDeliveryStatus();
        this.deliveryBoyId = order.getDeliveryBoyId();

        if (order.getOrderItems() != null) {
            this.items = order.getOrderItems().stream()
                .map(OrderItemDTO::new)
                .toList();
        }

        // TODO: Populate userContact
        // Option 1: If you store user's contact in Order entity, map here
        // Option 2: Fetch from UserService or userClient (better approach, async or via Feign)
        // For example, userContact = userClient.getUserContactById(order.getUserId());
        // For now, set as null or empty string
        this.userContact = null;
    }

    // Getters and Setters for new fields
    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public Long getDeliveryBoyId() {
        return deliveryBoyId;
    }

    public void setDeliveryBoyId(Long deliveryBoyId) {
        this.deliveryBoyId = deliveryBoyId;
    }

    public String getUserContact() {
        return userContact;
    }

    public void setUserContact(String userContact) {
        this.userContact = userContact;
    }

    // ... existing getters/setters here ...
}
