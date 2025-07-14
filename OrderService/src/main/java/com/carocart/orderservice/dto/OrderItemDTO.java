package com.carocart.orderservice.dto;

public class OrderItemDTO {
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double price;
    private Double totalPrice;
    private String productImageUrl; // <-- NEW FIELD

    public OrderItemDTO() {}

    // Recommended: Only use this in service, not in DTO constructor
    public OrderItemDTO(com.carocart.orderservice.entity.OrderItem item, String productImageUrl) {
        this.productId = item.getProductId();
        this.productName = item.getProductName();
        this.quantity = item.getQuantity();
        this.price = item.getPrice();
        this.totalPrice = item.getTotalPrice();
        this.productImageUrl = productImageUrl;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

    public String getProductImageUrl() { return productImageUrl; }
    public void setProductImageUrl(String productImageUrl) { this.productImageUrl = productImageUrl; }
}
