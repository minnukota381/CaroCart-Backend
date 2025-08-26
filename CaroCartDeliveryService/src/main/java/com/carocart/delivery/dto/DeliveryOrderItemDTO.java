package com.carocart.delivery.dto;

public class DeliveryOrderItemDTO {
    private Long orderId;

    // Card summary (order-level)
    private String productName;        // first item name or compact summary
    private Integer quantity;          // total quantity (or first item qty)

    // Address & parties
    private String deliveryAddress;
    private String userName;           // "First Last"
    private String userPhone;          // customer phone
    private String paymentType;        // COD / PAID etc.
    private String deliveryStatus;     // ASSIGNED / PICKED_UP / OUT_FOR_DELIVERY / DELIVERED

    // Vendor info needed by "View location" and "Call vendor"
    private Double vendorLat;
    private Double vendorLng;
    private String vendorPhone;

    // Getters & Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    public Double getVendorLat() { return vendorLat; }
    public void setVendorLat(Double vendorLat) { this.vendorLat = vendorLat; }

    public Double getVendorLng() { return vendorLng; }
    public void setVendorLng(Double vendorLng) { this.vendorLng = vendorLng; }

    public String getVendorPhone() { return vendorPhone; }
    public void setVendorPhone(String vendorPhone) { this.vendorPhone = vendorPhone; }
}
