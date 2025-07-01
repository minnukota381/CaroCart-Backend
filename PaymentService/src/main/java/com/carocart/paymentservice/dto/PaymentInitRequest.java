package com.carocart.paymentservice.dto;

import java.util.List;

public class PaymentInitRequest {
    private Double amount;
    private Long userId;
    private Long addressId;
    private List<CartItemDTO> cartItems; // You may have your own CartItemDTO
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Long getAddressId() {
		return addressId;
	}
	public void setAddressId(Long addressId) {
		this.addressId = addressId;
	}
	public List<CartItemDTO> getCartItems() {
		return cartItems;
	}
	public void setCartItems(List<CartItemDTO> cartItems) {
		this.cartItems = cartItems;
	}

    // Getters, setters, constructors
    // (Add as needed or use Lombok if you like)
    
}
