package com.carocart.delivery.dto;

public class DeliveryOrderDTO {
    private Long orderId;
    private String deliveryStatus;
    private String address;
    private String userContact;

    public DeliveryOrderDTO() {}

    public DeliveryOrderDTO(Long orderId, String deliveryStatus, String address, String userContact) {
        this.orderId = orderId;
        this.deliveryStatus = deliveryStatus;
        this.address = address;
        this.userContact = userContact;
    }

    // Getters and setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getUserContact() { return userContact; }
    public void setUserContact(String userContact) { this.userContact = userContact; }
}
