package com.carocart.delivery.dto;

public class DeliveryOrderDTO {
    private Long orderId;
    private String deliveryStatus;
    private String address;
    private String userContact;
    private String customerName;
    private String paymentType; // COD / Prepaid
    private Boolean otpSent;    // true if OTP was generated and sent
    private String deliveryTimeWindow; // optional
    // private List<OrderItemDTO> items; // optional, for detailed UI

    public DeliveryOrderDTO() {}

    // Constructor with new fields
    public DeliveryOrderDTO(
        Long orderId,
        String deliveryStatus,
        String address,
        String userContact,
        String customerName,
        String paymentType,
        Boolean otpSent,
        String deliveryTimeWindow
    ) {
        this.orderId = orderId;
        this.deliveryStatus = deliveryStatus;
        this.address = address;
        this.userContact = userContact;
        this.customerName = customerName;
        this.paymentType = paymentType;
        this.otpSent = otpSent;
        this.deliveryTimeWindow = deliveryTimeWindow;
    }

    // Getters and Setters

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getUserContact() { return userContact; }
    public void setUserContact(String userContact) { this.userContact = userContact; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public Boolean getOtpSent() { return otpSent; }
    public void setOtpSent(Boolean otpSent) { this.otpSent = otpSent; }

    public String getDeliveryTimeWindow() { return deliveryTimeWindow; }
    public void setDeliveryTimeWindow(String deliveryTimeWindow) { this.deliveryTimeWindow = deliveryTimeWindow; }
}
