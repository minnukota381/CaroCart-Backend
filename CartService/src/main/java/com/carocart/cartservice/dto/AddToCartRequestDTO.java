package com.carocart.cartservice.dto;

public class AddToCartRequestDTO {
    private Long productId;
    private int quantity;

    public AddToCartRequestDTO() {}

    public AddToCartRequestDTO(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
