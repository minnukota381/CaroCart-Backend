package com.carocart.orderservice.dto;

public class OrderItemDTO {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double price;
    private Double totalPrice;
    private String approvalRequiredBy;
    private String approvalStatus;
    private String vendorEmail;

    // DELIVERY FIELDS
    private String deliveryAddress;
    private String userName;
    private String userPhone;
    private String paymentType;
    private String deliveryStatus;

    public OrderItemDTO() {}

    // Entity-to-DTO
    public OrderItemDTO(com.carocart.orderservice.entity.OrderItem item) {
        this.id = item.getId();
        this.productId = item.getProductId();
        this.productName = item.getProductName();
        this.quantity = item.getQuantity();
        this.price = item.getPrice();
        this.totalPrice = item.getTotalPrice();
        this.approvalRequiredBy = item.getApprovalRequiredBy();
        this.approvalStatus = item.getApprovalStatus() != null ? item.getApprovalStatus().name() : null;
        this.vendorEmail = item.getVendorEmail();

        try {
            if (item.getOrder() != null) {
                this.orderId = item.getOrder().getId();
                this.deliveryAddress = item.getOrder().getShippingAddress();
                this.paymentType = item.getOrder().getPaymentStatus();
                this.deliveryStatus = item.getDeliveryStatus();
                // User info (safe fallback)
                this.userName = "N/A";
                this.userPhone = "N/A";
            }
        } catch (Exception e) {
            this.deliveryAddress = null;
            this.paymentType = null;
            this.deliveryStatus = null;
            this.userName = "N/A";
            this.userPhone = "N/A";
        }
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
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

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(Double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public String getApprovalRequiredBy() {
		return approvalRequiredBy;
	}

	public void setApprovalRequiredBy(String approvalRequiredBy) {
		this.approvalRequiredBy = approvalRequiredBy;
	}

	public String getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	public String getVendorEmail() {
		return vendorEmail;
	}

	public void setVendorEmail(String vendorEmail) {
		this.vendorEmail = vendorEmail;
	}

	public String getDeliveryAddress() {
		return deliveryAddress;
	}

	public void setDeliveryAddress(String deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
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

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getDeliveryStatus() {
		return deliveryStatus;
	}

	public void setDeliveryStatus(String deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}

    // --- Getters and Setters (all fields) ---
    // ... (You can keep your IDE to generate all the getters and setters)
}
