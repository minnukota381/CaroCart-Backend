package com.carocart.cartservice.dto;

public class ProductDTO {
    private Long id;
    private String name;
    private double price;
    private String imageUrl;
    private int stock;
    private boolean available;
    private Long vendorId;

    public ProductDTO() {}

    public ProductDTO(Long id, String name, double price, String imageUrl, int stock, boolean available, Long vendorId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.stock = stock;
        this.available = available;
        this.vendorId = vendorId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getStock() {
        return stock;
    }

    public boolean isAvailable() {
        return available;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }
}
