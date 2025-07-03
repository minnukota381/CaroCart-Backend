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
    
 // Delivery management
    List<OrderResponse> getReadyForDeliveryOrders();
    void assignOrderToDeliveryBoy(Long orderId, Long deliveryBoyId);
    List<OrderResponse> getAssignedOrders(Long deliveryBoyId);
    void updateDeliveryStatus(Long orderId, String status);


}
