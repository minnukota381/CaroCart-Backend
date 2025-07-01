package com.carocart.paymentservice.dto;

public class CartItemDTO {
    private Long productId;
    private Integer quantity;
    private Double price;
    // Any other needed fields
	public Long getProductId() {
		return productId;
	}
	public void setProductId(Long productId) {
		this.productId = productId;
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

    // Getters, setters, constructors
    
}
