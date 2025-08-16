package com.carocart.productservice.dto;

public class RatingEligibilityDTO {
    private Long orderItemId;
    private Long orderId;
    private Long userId;
    private Long productId;
    private String itemStatus;
    private String orderStatus;

    // Extra fields for client-friendly checks
    private Boolean eligible;   // true if user can rate this orderItem now
    private String reason;      // e.g., "OK", "Already rated", "Item not delivered", "Product mismatch"

    // --- Getters / Setters ---

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

    public Boolean getEligible() { return eligible; }
    public void setEligible(Boolean eligible) { this.eligible = eligible; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
