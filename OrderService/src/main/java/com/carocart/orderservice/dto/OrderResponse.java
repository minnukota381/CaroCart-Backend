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

    // Items (still included for detail pages)
    private List<OrderItemDTO> items;

    // Delivery (order-level)
    private Long deliveryBoyId;
    private String deliveryStatus;
    private Long vendorId;

    // ✅ FE needs these:
    private Double addressLat;
    private Double addressLng;
    private Double deliveryFee;
    private Double finalAmount;

    // ✅ Enrichment for Delivery app
    private String userName;     // "First Last"
    private String userPhone;    // customer phone
    private Double vendorLat;    // vendor coords
    private Double vendorLng;
    private String vendorPhone;  // vendor contact

    // ✅ Optional compact product summary for the card
    private String productName;  // sample/first item name
    private Integer quantity;    // sum qty across items (or first item)

    public OrderResponse() {}

    public OrderResponse(com.carocart.orderservice.entity.Order order) {
        this.orderId = order.getId();
        this.userId = order.getUserId();
        this.shippingAddress = order.getShippingAddress();
        this.addressId = order.getAddressId();
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();
        this.paymentStatus = order.getPaymentStatus();
        this.paymentId = order.getPaymentId();
        this.orderDate = order.getOrderDate();
        this.updatedAt = order.getUpdatedAt();
        this.deliveryBoyId = order.getDeliveryBoyId();
        this.deliveryStatus = order.getDeliveryStatus();
        this.vendorId = order.getVendorId();
        this.deliveryFee = order.getDeliveryFee();
        this.finalAmount = order.getFinalAmount();

        if (order.getOrderItems() != null) {
            this.items = order.getOrderItems().stream()
                    .map(OrderItemDTO::new)
                    .toList();

            // Compact product summary (first item name + total qty)
            String firstName = order.getOrderItems().get(0).getProductName();
            int totalQty = order.getOrderItems().stream()
                    .mapToInt(i -> i.getQuantity() == null ? 0 : i.getQuantity())
                    .sum();
            this.productName = firstName;
            this.quantity = totalQty;
        }
    }

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public Long getAddressId() {
		return addressId;
	}

	public void setAddressId(Long addressId) {
		this.addressId = addressId;
	}

	public Double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	public LocalDateTime getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(LocalDateTime orderDate) {
		this.orderDate = orderDate;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public List<OrderItemDTO> getItems() {
		return items;
	}

	public void setItems(List<OrderItemDTO> items) {
		this.items = items;
	}

	public Long getDeliveryBoyId() {
		return deliveryBoyId;
	}

	public void setDeliveryBoyId(Long deliveryBoyId) {
		this.deliveryBoyId = deliveryBoyId;
	}

	public String getDeliveryStatus() {
		return deliveryStatus;
	}

	public void setDeliveryStatus(String deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}

	public Long getVendorId() {
		return vendorId;
	}

	public void setVendorId(Long vendorId) {
		this.vendorId = vendorId;
	}

	public Double getAddressLat() {
		return addressLat;
	}

	public void setAddressLat(Double addressLat) {
		this.addressLat = addressLat;
	}

	public Double getAddressLng() {
		return addressLng;
	}

	public void setAddressLng(Double addressLng) {
		this.addressLng = addressLng;
	}

	public Double getDeliveryFee() {
		return deliveryFee;
	}

	public void setDeliveryFee(Double deliveryFee) {
		this.deliveryFee = deliveryFee;
	}

	public Double getFinalAmount() {
		return finalAmount;
	}

	public void setFinalAmount(Double finalAmount) {
		this.finalAmount = finalAmount;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}

	public Double getVendorLat() {
		return vendorLat;
	}

	public void setVendorLat(Double vendorLat) {
		this.vendorLat = vendorLat;
	}

	public Double getVendorLng() {
		return vendorLng;
	}

	public void setVendorLng(Double vendorLng) {
		this.vendorLng = vendorLng;
	}

	public String getVendorPhone() {
		return vendorPhone;
	}

	public void setVendorPhone(String vendorPhone) {
		this.vendorPhone = vendorPhone;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

    // Getters & setters (include the new ones!)
    // ... generate for all fields above ...
}
