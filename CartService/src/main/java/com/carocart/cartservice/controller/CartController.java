package com.carocart.cartservice.controller;

import com.carocart.cartservice.dto.AddToCartRequestDTO;
import com.carocart.cartservice.dto.CartItemDTO;
import com.carocart.cartservice.dto.CartItemResponse;
import com.carocart.cartservice.dto.CartSummaryDTO;
import com.carocart.cartservice.dto.UpdateCartRequestDTO;
import com.carocart.cartservice.service.CartService;
import com.carocart.cartservice.util.JwtUtil;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;
    @Autowired
private JwtUtil jwtUtil;
    @GetMapping("/get")
    public ResponseEntity<List<CartItemDTO>> getCartItemsByToken(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(cartService.getCartItemsByToken(token));
    }

    @PostMapping("/add")
    public ResponseEntity<CartItemDTO> addToCart(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody AddToCartRequestDTO request) {

        return ResponseEntity.ok(cartService.addToCart(token, request));
    }

    @PutMapping("/update")
    public ResponseEntity<CartItemDTO> updateCart(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody UpdateCartRequestDTO request) {

        return ResponseEntity.ok(
            cartService.updateCartItem(token, request.getProductId(), request.getQuantity())
        );
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Void> removeItem(
            @RequestHeader("Authorization") String token,
            @PathVariable Long productId) {

        cartService.removeCartItem(token, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(
            @RequestHeader("Authorization") String token) {

        cartService.clearCart(token);
        return ResponseEntity.noContent().build();
    }
    
//    @GetMapping("/summary")
//    public ResponseEntity<CartSummaryDTO> getCartSummary(@RequestHeader("Authorization") String token) {
//        Long userId = jwtUtil.extractUserId(token);
//        return ResponseEntity.ok(cartService.getCartSummary(userId));
//    }

}
