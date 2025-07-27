package com.carocart.cartservice.dto;

public class CartItemDTO {
    private Long id;
    private Long userId;
    private Long productId;
    private int quantity;
    private String productName;
    private double price;
    private String imageUrl;
    private Long vendorId; // <--- ADDED THIS FIELD

    public CartItemDTO() {}

    // No-arg constructor is good practice for DTOs for JSON deserialization

    // Getters
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getProductName() {
        return productName;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Long getVendorId() { // <--- ADDED THIS GETTER
        return vendorId;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setVendorId(Long vendorId) { // <--- ADDED THIS SETTER
        this.vendorId = vendorId;
    }
}