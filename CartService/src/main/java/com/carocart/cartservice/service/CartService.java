package com.carocart.cartservice.service;

import com.carocart.cartservice.dto.AddToCartRequestDTO;
import com.carocart.cartservice.dto.CartItemDTO; // Make sure this is imported
// import com.carocart.cartservice.dto.CartItemResponse; // You can comment out or remove this line if CartItemResponse is no longer needed

import java.util.List;

public interface CartService {
//	List<CartItemDTO> getCartItemsByUserId(Long userId); // Keep commented or remove based on your needs

	CartItemDTO addToCart(String token, AddToCartRequestDTO request);

	CartItemDTO updateCartItem(String token, Long productId, int quantity);

	void removeCartItem(String token, Long productId);

	void clearCart(String token);

	// THIS IS THE LINE THAT NEEDS TO BE CHANGED
	List<CartItemDTO> getCartItemsByToken(String token); // Changed from CartItemResponse to CartItemDTO

}