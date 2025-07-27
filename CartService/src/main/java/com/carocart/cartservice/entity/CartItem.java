package com.carocart.cartservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cart_item") // Good practice to explicitly define table name
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long productId;

    // REMOVED FIELDS: productName, quantity, price, imageUrl, vendorId
    // The quantity is the only product-specific detail that needs to stay here,
    // as it represents the count of THIS product in THIS cart.

    private int quantity; // This must remain as it's specific to the cart item itself

    // Default no-arg constructor required by JPA
    public CartItem() {
    }

    // Convenience constructor
    public CartItem(Long userId, Long productId, int quantity) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    // Only 'quantity' remains from the product-specific details
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // REMOVED Getters and Setters for:
    // getProductName(), setProductName()
    // getPrice(), setPrice()
    // getImageUrl(), setImageUrl()
    // getVendorId(), setVendorId()
}