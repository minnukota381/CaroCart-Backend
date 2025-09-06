package com.carocart.orderservice.service;

import com.carocart.orderservice.dto.OrderItemDTO;
import com.carocart.orderservice.dto.OrderRequest;
import com.carocart.orderservice.dto.OrderResponse;
import com.carocart.orderservice.dto.VendorEarningsDTO;
import com.carocart.orderservice.dto.VendorPayoutPreviewDTO;
import com.carocart.orderservice.dto.VendorPayoutResponseDTO;
import com.carocart.orderservice.entity.Order;

import java.util.List;
import java.util.Map;

public interface OrderService {
    // Vendor views
    List<OrderItemDTO> getAllItemsForVendor(String token);
    List<OrderItemDTO> getPendingItemsForVendor(String token);
    void approveItemAsVendor(String token, Long itemId);
    void rejectItemAsVendor(String token, Long itemId);
    void approveOrderItem(Long itemId, String vendorEmail);
    void rejectOrderItem(Long itemId, String vendorEmail);

    // Delivery (ORDER LEVEL)
    List<OrderResponse> getOrdersAvailableForDelivery(String token);
    void assignOrderToDeliveryBoy(String token, Long orderId);
    void updateOrderDeliveryStatus(String token, Long orderId, String status);
    List<OrderResponse> getAssignedOrdersForRider(String token);
    Map<String, Object> getAssignedDeliveryBoyLocation(Long orderId);

    // User
    OrderResponse placeOrder(String token, OrderRequest orderRequest, String couponCode);
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
    OrderResponse placeOrderInternal(OrderRequest orderRequest, String couponCode);
    Order getOrderEntity(Long orderId);
    // Internal token for Feign use
    String getInternalToken();

    // Admin-side item approval
    List<OrderItemDTO> getPendingItemsForAdmin();
    void approveOrderByAdmin(Long orderId);
    void rejectOrderByAdmin(Long orderId);

    // Earnings
    List<VendorEarningsDTO> getVendorEarnings(String fromDate, String toDate);
    List<VendorEarningsDTO> getVendorEarningsByPeriod(String period);
    VendorEarningsDTO getMyVendorEarnings(String token, String period);
 // OrderService.java
    VendorPayoutPreviewDTO previewVendorUnpaid(Long vendorId);
    VendorPayoutResponseDTO payVendorUnpaid(Long vendorId, String note);

}
