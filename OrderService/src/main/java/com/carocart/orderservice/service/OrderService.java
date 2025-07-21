package com.carocart.orderservice.service;

import com.carocart.orderservice.dto.OrderRequest;
import com.carocart.orderservice.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    // --- User Functionality ---
    OrderResponse placeOrder(String token, OrderRequest orderRequest);
    List<OrderResponse> getOrdersForUser(String token);
    OrderResponse getOrderById(String token, Long orderId);
    void cancelOrder(String token, Long orderId);

    // --- Admin Functionality ---
    List<OrderResponse> getAllOrders();
    OrderResponse getOrderByIdForAdmin(Long orderId);
    void cancelOrderByAdmin(Long orderId);

    // --- Payment Updates ---
    void updatePaymentStatus(Long orderId, String paymentId, String status);  // For online/UPI/card payments
    void markAsCod(Long orderId); // For Cash on Delivery
    void markOrderAsShipped(Long orderId);
    
    long getTotalOrdersCount();
    OrderResponse placeOrderInternal(OrderRequest orderRequest);

    // --- Approval Handling ---
    java.util.List<com.carocart.orderservice.dto.OrderItemDTO> getPendingItemsForVendor(String token);
    java.util.List<com.carocart.orderservice.dto.OrderItemDTO> getPendingItemsForAdmin();
    void approveItemAsVendor(String token, Long itemId);
    void approveItemAsAdmin(Long itemId);
    void rejectItemAsVendor(String token, Long itemId);
    void rejectItemAsAdmin(Long itemId);

}