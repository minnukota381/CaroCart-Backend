package com.carocart.orderservice.controller;

import com.carocart.orderservice.dto.OrderRequest;
import com.carocart.orderservice.dto.OrderResponse;
import com.carocart.orderservice.dto.PaymentUpdateRequestDTO;
import com.carocart.orderservice.service.OrderService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ----------------- USER APIs -----------------
 // --------- PaymentService Integration Endpoint ---------

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
        OrderResponse response = orderService.placeOrderInternal(orderRequest);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/internal/place")
    public ResponseEntity<OrderResponse> placeOrderInternal(@RequestBody OrderRequest orderRequest) {
        // ADD THIS LOG
        System.out.println("OrderRequest received: " +
            "userId=" + orderRequest.getUserId() +
            ", addressId=" + orderRequest.getAddressId() +
            ", shippingAddress=" + orderRequest.getShippingAddress() +
            ", items=" + orderRequest.getItems() +
            ", totalAmount=" + orderRequest.getTotalAmount()
        );
        OrderResponse response = orderService.placeOrderInternal(orderRequest);
        return ResponseEntity.ok(response);
    }

    // For user checkout (JWT required)
    @PostMapping("/place")
    public ResponseEntity<OrderResponse> placeOrder(@RequestHeader("Authorization") String token,
                                                    @RequestBody OrderRequest orderRequest) {
        OrderResponse response = orderService.placeOrder(token, orderRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@RequestHeader("Authorization") String token) {
        List<OrderResponse> orders = orderService.getOrdersForUser(token);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId,
                                                      @RequestHeader("Authorization") String token) {
        OrderResponse response = orderService.getOrderById(token, orderId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/cancel/{orderId}")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId,
                                              @RequestHeader("Authorization") String token) {
        orderService.cancelOrder(token, orderId);
        return ResponseEntity.ok("Order cancelled successfully.");
    }

    @PutMapping("/{orderId}/payment")
    public ResponseEntity<String> updatePaymentStatus(@PathVariable Long orderId,
                                                      @RequestBody PaymentUpdateRequestDTO request) {
        if (request.getPaymentId() == null || request.getPaymentId().isBlank()
                || request.getStatus() == null || request.getStatus().isBlank()) {
            return ResponseEntity.badRequest().body("Payment ID and status must not be blank");
        }

        try {
            orderService.updatePaymentStatus(orderId, request.getPaymentId(), request.getStatus());
            return ResponseEntity.ok("Payment status updated successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error updating payment status: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error while updating payment status.");
        }
    }

    @PutMapping("/{orderId}/cod")
    public ResponseEntity<String> markOrderAsCod(@PathVariable Long orderId) {
        try {
            orderService.markAsCod(orderId);
            return ResponseEntity.ok("COD order placed successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error in COD placement: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error during COD order placement.");
        }
    }

    @GetMapping("/debug/token")
    public ResponseEntity<String> debugToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return ResponseEntity.ok("Authorization header = " + authHeader);
    }

    // ----------------- ADMIN APIs -----------------

    @GetMapping("/admin/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrdersForAdmin() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/admin/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOrderByIdForAdmin(@PathVariable Long orderId) {
        OrderResponse order = orderService.getOrderByIdForAdmin(orderId);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/admin/orders/{orderId}/cancel")
    public ResponseEntity<String> cancelOrderByAdmin(@PathVariable Long orderId) {
        orderService.cancelOrderByAdmin(orderId);
        return ResponseEntity.ok("Order cancelled by Admin successfully.");
    }

    @PutMapping("/admin/orders/{orderId}/ship")
    public ResponseEntity<String> shipOrder(@PathVariable Long orderId) {
        orderService.markOrderAsShipped(orderId);
        return ResponseEntity.ok("Order marked as shipped.");
    }
    
 // For admin dashboard stats: total order count
    
    @GetMapping("/admin/orders/count")
    public ResponseEntity<Long> getTotalOrdersCount() {
        long count = orderService.getTotalOrdersCount();
        return ResponseEntity.ok(count);
    }
    
 // --- DELIVERY SERVICE ENDPOINTS ---

 // 1. Get all ready-for-delivery (unassigned) orders
 @GetMapping("/ready-for-delivery")
 public ResponseEntity<List<OrderResponse>> getReadyForDeliveryOrders() {
     List<OrderResponse> readyOrders = orderService.getReadyForDeliveryOrders();
     
     return ResponseEntity.ok(readyOrders);
 }

 // 2. Assign order to delivery boy (self-assignment)
 @PutMapping("/{orderId}/assign-delivery")
 public ResponseEntity<String> assignOrderToDeliveryBoy(
         @PathVariable Long orderId,
         @RequestParam Long deliveryBoyId) {
     orderService.assignOrderToDeliveryBoy(orderId, deliveryBoyId);
     return ResponseEntity.ok("Order assigned to delivery boy.");
 }

 // 3. Get all assigned orders for a delivery boy
 @GetMapping("/assigned/{deliveryBoyId}")
 public ResponseEntity<List<OrderResponse>> getAssignedOrders(@PathVariable Long deliveryBoyId) {
     List<OrderResponse> assigned = orderService.getAssignedOrders(deliveryBoyId);
     return ResponseEntity.ok(assigned);
 }

 // 4. Delivery boy updates delivery status (OUT_FOR_DELIVERY, DELIVERED, etc)
 @PutMapping("/{orderId}/delivery-status")
 public ResponseEntity<String> updateDeliveryStatus(
         @PathVariable Long orderId,
         @RequestParam String status) {
     orderService.updateDeliveryStatus(orderId, status);
     return ResponseEntity.ok("Delivery status updated.");
 }


}
