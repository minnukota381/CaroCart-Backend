package com.carocart.cartservice.service;

import com.carocart.cartservice.dto.AddToCartRequestDTO;
import com.carocart.cartservice.dto.CartItemDTO;
import com.carocart.cartservice.dto.ProductDTO;
import com.carocart.cartservice.dto.UserResponseDTO;
import com.carocart.cartservice.entity.CartItem;
import com.carocart.cartservice.exception.OneVendorCartException;
import com.carocart.cartservice.feign.ProductClient;
import com.carocart.cartservice.feign.UserClient;
import com.carocart.cartservice.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserClient userClient;

    @Autowired
    private ProductClient productClient;

    @Override
    public CartItemDTO addToCart(String token, AddToCartRequestDTO request) {
        UserResponseDTO user = userClient.getCurrentUser(token);
        Long userId = user.getId();

        // --- 1. Validate new product ---
        ProductDTO productToAdd = fetchProductOrThrow(request.getProductId());

        if (productToAdd.getVendorId() == null) {
            throw new RuntimeException("Product vendor information is missing for product ID: " + request.getProductId());
        }
        if (!productToAdd.isAvailable()) {
            throw new RuntimeException("Product is not available.");
        }
        if (request.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be positive.");
        }
        if (request.getQuantity() > productToAdd.getStock()) {
            throw new RuntimeException("Requested quantity (" + request.getQuantity()
                    + ") exceeds available stock (" + productToAdd.getStock() + ").");
        }

        // --- 2. Vendor restriction check ---
        List<CartItem> existingCartItems = cartItemRepository.findByUserId(userId);
        if (!existingCartItems.isEmpty()) {
            ProductDTO existingProductInCart = fetchProductOrThrow(existingCartItems.get(0).getProductId());

            if (existingProductInCart.getVendorId() == null) {
                throw new RuntimeException("Existing cart item has missing vendor information. Please clear cart.");
            }

            if (!productToAdd.getVendorId().equals(existingProductInCart.getVendorId())) {
                throw new OneVendorCartException("You can only add items from one vendor at a time.");
            }
        }

        // --- 3. Save / update cart item ---
        Optional<CartItem> existingItemOpt =
                cartItemRepository.findByUserIdAndProductId(userId, request.getProductId());

        CartItem item;
        if (existingItemOpt.isPresent()) {
            item = existingItemOpt.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            item = new CartItem(userId, request.getProductId(), request.getQuantity());
        }

        CartItem saved = cartItemRepository.save(item);
        return convertToDTO(saved);
    }

    @Override
    public CartItemDTO updateCartItem(String token, Long productId, int quantity) {
        UserResponseDTO user = userClient.getCurrentUser(token);
        Long userId = user.getId();

        ProductDTO productToUpdate = fetchProductOrThrow(productId);

        if (!productToUpdate.isAvailable()) {
            throw new RuntimeException("Product is not available.");
        }
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be positive. Use remove endpoint to remove an item.");
        }
        if (quantity > productToUpdate.getStock()) {
            throw new RuntimeException("Requested quantity (" + quantity
                    + ") exceeds available stock (" + productToUpdate.getStock() + ").");
        }

        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new RuntimeException("Item not found in cart."));

        item.setQuantity(quantity);
        CartItem saved = cartItemRepository.save(item);
        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public void removeCartItem(String token, Long productId) {
        UserResponseDTO user = userClient.getCurrentUser(token);
        cartItemRepository.deleteByUserIdAndProductId(user.getId(), productId);
    }

    @Override
    public void clearCart(String token) {
        UserResponseDTO user = userClient.getCurrentUser(token);
        List<CartItem> items = cartItemRepository.findByUserId(user.getId());
        cartItemRepository.deleteAll(items);
    }

    @Override
    public List<CartItemDTO> getCartItemsByToken(String token) {
        UserResponseDTO user = userClient.getCurrentUser(token);
        List<CartItem> items = cartItemRepository.findByUserId(user.getId());

        return items.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ---------------- Private Helpers ----------------

    private ProductDTO fetchProductOrThrow(Long productId) {
        try {
            ProductDTO product = productClient.getProductDTOById(productId);
            if (product == null || product.getId() == null) {
                throw new RuntimeException();
            }
            return product;
        } catch (Exception e) {
            throw new RuntimeException("Product not found or unavailable for ID: " + productId);
        }
    }

    private CartItemDTO convertToDTO(CartItem cartItem) {
        ProductDTO productDetails = fetchProductOrThrow(cartItem.getProductId());

        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setUserId(cartItem.getUserId());
        dto.setProductId(cartItem.getProductId());
        dto.setQuantity(cartItem.getQuantity());

        dto.setProductName(productDetails.getName());
        dto.setPrice(productDetails.getPrice());
        dto.setImageUrl(productDetails.getImageUrl());
        dto.setVendorId(productDetails.getVendorId());

        return dto;
    }
}
