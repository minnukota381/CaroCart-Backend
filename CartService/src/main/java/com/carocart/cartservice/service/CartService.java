package com.carocart.cartservice.service;

import com.carocart.cartservice.dto.AddToCartRequestDTO;
import com.carocart.cartservice.dto.CartItemDTO;

import java.util.List;

public interface CartService {

    CartItemDTO addToCart(String token, AddToCartRequestDTO request);

    CartItemDTO updateCartItem(String token, Long productId, int quantity);

    void removeCartItem(String token, Long productId);

    void clearCart(String token);

    List<CartItemDTO> getCartItemsByToken(String token);
}
