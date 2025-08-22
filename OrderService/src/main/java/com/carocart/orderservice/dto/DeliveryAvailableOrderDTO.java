package com.carocart.orderservice.dto;

public class DeliveryAvailableOrderDTO {

    private Long orderId;

    // display
    private String productSummary;   // e.g. "Potato × 4 + 2 more"
    private Integer totalQuantity;

    // customer
    private String userName;
    private String userPhone;
    private String deliveryAddress;

    // payment
    private String paymentType;      // "PAID"/"COD"/"PENDING"

    // locations
    private Double vendorLat;
    private Double vendorLng;
    private Double userLat;
    private Double userLng;

    // helpful extras
    private Double distanceKm;       // vendor → user
    private Double riderPayout;      // slab payout (₹19/29/39/59)
    private String vendorPhone;

    // --- Constructors ---

    public DeliveryAvailableOrderDTO() {
    }

    // Match the exact constructor used in service code
    public DeliveryAvailableOrderDTO(
            Long orderId,
            String productSummary,
            int totalQuantity,
            String userName,
            String userPhone,
            String deliveryAddress,
            String paymentType,
            Double vendorLat,
            Double vendorLng,
            Double userLat,
            Double userLng,
            double distanceKm,
            double riderPayout,
            String vendorPhone
    ) {
        this.orderId = orderId;
        this.productSummary = productSummary;
        this.totalQuantity = totalQuantity;   // autobox to Integer
        this.userName = userName;
        this.userPhone = userPhone;
        this.deliveryAddress = deliveryAddress;
        this.paymentType = paymentType;
        this.vendorLat = vendorLat;
        this.vendorLng = vendorLng;
        this.userLat = userLat;
        this.userLng = userLng;
        this.distanceKm = distanceKm;         // autobox to Double
        this.riderPayout = riderPayout;       // autobox to Double
        this.vendorPhone = vendorPhone;
    }

    // --- Getters & Setters ---

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getProductSummary() { return productSummary; }
    public void setProductSummary(String productSummary) { this.productSummary = productSummary; }

    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public Double getVendorLat() { return vendorLat; }
    public void setVendorLat(Double vendorLat) { this.vendorLat = vendorLat; }

    public Double getVendorLng() { return vendorLng; }
    public void setVendorLng(Double vendorLng) { this.vendorLng = vendorLng; }

    public Double getUserLat() { return userLat; }
    public void setUserLat(Double userLat) { this.userLat = userLat; }

    public Double getUserLng() { return userLng; }
    public void setUserLng(Double userLng) { this.userLng = userLng; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Double getRiderPayout() { return riderPayout; }
    public void setRiderPayout(Double riderPayout) { this.riderPayout = riderPayout; }

    public String getVendorPhone() { return vendorPhone; }
    public void setVendorPhone(String vendorPhone) { this.vendorPhone = vendorPhone; }
}
