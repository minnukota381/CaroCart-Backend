package com.carocart.cartservice.service;

import com.carocart.cartservice.dto.AddToCartRequestDTO;
import com.carocart.cartservice.dto.CartItemDTO; // Assuming this is your response DTO for a single item
import com.carocart.cartservice.dto.ProductDTO;   // The DTO from Product Service
import com.carocart.cartservice.dto.UserResponseDTO;
import com.carocart.cartservice.entity.CartItem;
import com.carocart.cartservice.exception.OneVendorCartException;
import com.carocart.cartservice.feign.ProductClient;
import com.carocart.cartservice.feign.UserClient;
import com.carocart.cartservice.repository.CartItemRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList; // Added for explicit ArrayList instantiation
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

        // 1. Get product details for the NEW product being added
        ProductDTO productToAdd = fetchProductOrThrow(request.getProductId());

        // Basic product availability and stock checks
        if (productToAdd.getVendorId() == null) {
            throw new RuntimeException("Product vendor information is missing for product ID: " + request.getProductId());
        }

        // Assuming isAvailable() is the correct getter name in ProductDTO
        if (!productToAdd.isAvailable()) {
            throw new RuntimeException("Product is not available.");
        }

        if (request.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be positive.");
        }

        if (request.getQuantity() > productToAdd.getStock()) {
            throw new RuntimeException("Requested quantity (" + request.getQuantity() + ") exceeds available stock (" + productToAdd.getStock() + ").");
        }

        // 2. Vendor restriction check
        List<CartItem> existingCartItems = cartItemRepository.findByUserId(userId);
        if (!existingCartItems.isEmpty()) {
            // Fetch ProductDTO for the FIRST existing item to get its vendorId
            // Assuming all items in cart are from the same vendor, we only need to check the first one.
            ProductDTO existingProductInCart = fetchProductOrThrow(existingCartItems.get(0).getProductId());

            if (existingProductInCart.getVendorId() == null) {
                // This scenario means an existing cart item's product data is bad or changed.
                // You might want a more sophisticated way to handle this, like allowing
                // clearing the cart, or fetching product info for all items to validate.
                throw new RuntimeException("Existing cart item has missing vendor information. Please clear cart.");
            }

            // Compare vendorId from the NEW product with the EXISTING product's vendorId
            if (!productToAdd.getVendorId().equals(existingProductInCart.getVendorId())) {
                throw new OneVendorCartException("You can only add items from one vendor at a time.");
            }
        }

        // 3. Add or update cart item (CartItem entity now only holds userId, productId, quantity)
        Optional<CartItem> existingItemOpt = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId());
        CartItem item;
        if (existingItemOpt.isPresent()) {
            item = existingItemOpt.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            item = new CartItem(userId, request.getProductId(), request.getQuantity());
            // No need to set productName, price, imageUrl, vendorId on CartItem entity anymore
        }

        CartItem saved = cartItemRepository.save(item);
        // convertToDTO now solely relies on fetching product details from Product Service
        return convertToDTO(saved);
    }

    @Override
    public CartItemDTO updateCartItem(String token, Long productId, int quantity) {
        UserResponseDTO user = userClient.getCurrentUser(token);
        Long userId = user.getId();

        // Get product details for the product being updated
        ProductDTO productToUpdate = fetchProductOrThrow(productId);

        if (!productToUpdate.isAvailable()) {
            throw new RuntimeException("Product is not available.");
        }

        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be positive. Use remove endpoint to remove an item.");
        }

        if (quantity > productToUpdate.getStock()) {
            throw new RuntimeException("Requested quantity (" + quantity + ") exceeds available stock (" + productToUpdate.getStock() + ").");
        }

        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new RuntimeException("Item not found in cart."));

        item.setQuantity(quantity);
        CartItem saved = cartItemRepository.save(item);
        return convertToDTO(saved); // convertToDTO now solely relies on fetching product details
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
    public List<CartItemDTO> getCartItemsByToken(String token) { // Changed return type here
        UserResponseDTO user = userClient.getCurrentUser(token);
        List<CartItem> items = cartItemRepository.findByUserId(user.getId());

        return items.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ---------- Private Helpers ----------

    // Helper method to fetch ProductDTO or throw a specific runtime exception
    private ProductDTO fetchProductOrThrow(Long productId) {
        try {
            ProductDTO product = productClient.getProductDTOById(productId);
            // Additional check: Feign might return a null DTO if the product doesn't exist
            if (product == null || product.getId() == null) { // Check product.getId() to ensure it's a valid DTO
                throw new RuntimeException(); // Trigger catch block
            }
            return product;
        } catch (Exception e) {
            // Log the actual exception e for debugging purposes
            // Logger.error("Failed to fetch product " + productId, e);
            throw new RuntimeException("Product not found or unavailable for ID: " + productId);
        }
    }

    // New convertToDTO method that fetches product details from Product Service
    private CartItemDTO convertToDTO(CartItem cartItem) {
        // Fetch product details for the cartItem's productId
        ProductDTO productDetails = fetchProductOrThrow(cartItem.getProductId());

        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setUserId(cartItem.getUserId());
        dto.setProductId(cartItem.getProductId());
        dto.setQuantity(cartItem.getQuantity());

        // Populate from fetched ProductDTO
        dto.setProductName(productDetails.getName());
        dto.setPrice(productDetails.getPrice());
        dto.setImageUrl(productDetails.getImageUrl());
        // If CartItemDTO has a vendorId field for response, populate it:
        dto.setVendorId(productDetails.getVendorId());

        return dto;
    }
}