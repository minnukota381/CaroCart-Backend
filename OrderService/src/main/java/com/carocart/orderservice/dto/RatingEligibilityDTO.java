package com.carocart.orderservice.dto;

public class RatingEligibilityDTO {
    private Long orderItemId;
    private Long orderId;
    private Long userId;
    private Long productId;
    private String itemStatus;   // from OrderItem.deliveryStatus
    private String orderStatus;  // from Order.status

    public RatingEligibilityDTO() {}

    public RatingEligibilityDTO(Long orderItemId, Long orderId, Long userId, Long productId, String itemStatus, String orderStatus) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.itemStatus = itemStatus;
        this.orderStatus = orderStatus;
    }

    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getItemStatus() { return itemStatus; }
    public void setItemStatus(String itemStatus) { this.itemStatus = itemStatus; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
}
