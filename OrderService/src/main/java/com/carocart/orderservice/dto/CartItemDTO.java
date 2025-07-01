package com.carocart.orderservice.dto;

public class CartItemDTO {
    private Long productId;
    private String productName;  // Optional, but good for display
    private Integer quantity;
    private Double price;        // Per unit price

    public CartItemDTO() {}

    // Getters & Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}
