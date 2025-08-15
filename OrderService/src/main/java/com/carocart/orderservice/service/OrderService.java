package com.carocart.orderservice.service;

import com.carocart.orderservice.dto.OrderItemDTO;
import com.carocart.orderservice.dto.OrderRequest;
import com.carocart.orderservice.dto.OrderResponse;
import com.carocart.orderservice.dto.VendorEarningsDTO;
import com.carocart.orderservice.entity.Order;

import java.util.List;
import java.util.Map;

public interface OrderService {
    List<OrderItemDTO> getAllItemsForVendor(String token);
    List<OrderResponse> getOrdersAvailableForDelivery(String token);
    void assignOrderToDeliveryBoy(String token, Long orderId);
    void updateOrderDeliveryStatus(String token, Long orderId, String status);
    Map<String, Object> getAssignedDeliveryBoyLocation(Long orderId);

    // User
    OrderResponse placeOrder(String token, OrderRequest orderRequest);
    List<OrderResponse> getOrdersForUser(String token);
    OrderResponse getOrderById(String token, Long orderId);
    void cancelOrder(String token, Long orderId);

    // Admin
    List<OrderResponse> getAllOrders();
    OrderResponse getOrderByIdForAdmin(Long orderId);
    void cancelOrderByAdmin(Long orderId);

    // Payment
    void updatePaymentStatus(Long orderId, String paymentId, String status);
    void markAsCod(Long orderId);
    void markOrderAsShipped(Long orderId);
    long getTotalOrdersCount();
    OrderResponse placeOrderInternal(OrderRequest orderRequest);
    Order getOrderEntity(Long orderId);

    // Approval - Only Vendor
    List<OrderItemDTO> getPendingItemsForVendor(String token);
    void approveItemAsVendor(String token, Long itemId);
    void rejectItemAsVendor(String token, Long itemId);
    void approveOrderItem(Long itemId, String vendorEmail);
    void rejectOrderItem(Long itemId, String vendorEmail);
    List<OrderItemDTO> getAvailableOrderItemsForDelivery();
    void assignOrderItemToDeliveryBoy(Long orderItemId, Long deliveryBoyId);
    void updateOrderItemDeliveryStatus(Long orderItemId, String status);
    public List<VendorEarningsDTO> getVendorEarnings(String fromDate, String toDate);
    // ✅ Internal token for Feign use
    String getInternalToken();
    
 // Admin-side item approval
    List<OrderItemDTO> getPendingItemsForAdmin();
    void approveOrderByAdmin(Long orderId);
    void rejectOrderByAdmin(Long orderId);


}
