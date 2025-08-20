package com.carocart.cartservice.dto;

public class CartItemDTO {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private int quantity;
    private double price;
    private String imageUrl;
    private Long vendorId; // helpful for one-vendor-cart logic

    public CartItemDTO() {}

    // Getters/Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
}
