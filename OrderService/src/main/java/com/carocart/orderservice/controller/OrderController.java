package com.carocart.orderservice.controller;

import com.carocart.orderservice.dto.*;
import com.carocart.orderservice.entity.ApprovalStatus;
import com.carocart.orderservice.entity.Order;
import com.carocart.orderservice.entity.OrderItem;
import com.carocart.orderservice.feign.AddressClient;
import com.carocart.orderservice.feign.DeliveryBoyClient;
import com.carocart.orderservice.feign.ProductClient;
import com.carocart.orderservice.feign.UserClient;
import com.carocart.orderservice.feign.VendorClient;
import com.carocart.orderservice.repository.OrderItemRepository;
import com.carocart.orderservice.repository.OrderRepository;
import com.carocart.orderservice.service.OrderService;
import com.carocart.orderservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderItemRepository orderItemRepository;
    private final VendorClient vendorClient;
    private final DeliveryBoyClient deliveryBoyClient;
    private final JwtUtil jwtUtil;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final AddressClient addressClient;
    private final OrderRepository orderRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public OrderController(OrderService orderService,
                           OrderItemRepository orderItemRepository,
                           VendorClient vendorClient,
                           DeliveryBoyClient deliveryBoyClient,
                           JwtUtil jwtUtil,
                           UserClient userClient,
                           AddressClient addressClient,
                           ProductClient productClient,
                           OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderItemRepository = orderItemRepository;
        this.vendorClient = vendorClient;
        this.deliveryBoyClient = deliveryBoyClient;
        this.jwtUtil = jwtUtil;
        this.userClient = userClient;
        this.addressClient = addressClient;
        this.productClient = productClient;
        this.orderRepository = orderRepository;
    }

    // ----------------- USER APIs -----------------

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
        return ResponseEntity.ok(orderService.placeOrderInternal(orderRequest));
    }

    @PostMapping("/internal/place")
    public ResponseEntity<OrderResponse> placeOrderInternal(@RequestBody OrderRequest orderRequest) {
        return ResponseEntity.ok(orderService.placeOrderInternal(orderRequest));
    }

    @PostMapping("/place")
    public ResponseEntity<OrderResponse> placeOrder(@RequestHeader("Authorization") String token,
                                                    @RequestBody OrderRequest orderRequest) {
        return ResponseEntity.ok(orderService.placeOrder(token, orderRequest));
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(orderService.getOrdersForUser(token));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId,
                                                      @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(orderService.getOrderById(token, orderId));
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
        orderService.updatePaymentStatus(orderId, request.getPaymentId(), request.getStatus());
        return ResponseEntity.ok("Payment status updated successfully.");
    }

    @PutMapping("/{orderId}/cod")
    public ResponseEntity<String> markOrderAsCod(@PathVariable Long orderId) {
        orderService.markAsCod(orderId);
        return ResponseEntity.ok("COD order placed successfully.");
    }

    @GetMapping("/debug/token")
    public ResponseEntity<String> debugToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return ResponseEntity.ok("Authorization header = " + authHeader);
    }

    // ----------------- ADMIN APIs -----------------

    @GetMapping("/admin/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrdersForAdmin() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/admin/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOrderByIdForAdmin(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderByIdForAdmin(orderId));
    }

    @GetMapping("/admin/orders/count")
    public ResponseEntity<Long> getTotalOrdersCount() {
        return ResponseEntity.ok(orderService.getTotalOrdersCount());
    }

    // -------- Vendor APIs ---------

    @GetMapping("/vendor/pending-items")
    public ResponseEntity<List<OrderItemDTO>> getVendorPendingItems(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(orderService.getPendingItemsForVendor(token));
    }

    @PutMapping("/vendor/items/{itemId}/approve")
    public ResponseEntity<String> approveItemVendor(@RequestHeader("Authorization") String token,
                                                    @PathVariable Long itemId) {
        orderService.approveItemAsVendor(token, itemId);
        return ResponseEntity.ok("Item approved");
    }

    @PutMapping("/vendor/items/{itemId}/reject")
    public ResponseEntity<String> rejectItemVendor(@RequestHeader("Authorization") String token,
                                                   @PathVariable Long itemId) {
        orderService.rejectItemAsVendor(token, itemId);
        return ResponseEntity.ok("Item rejected");
    }

    @GetMapping("/vendor/orders")
    public ResponseEntity<List<OrderItemDTO>> getAllItemsForVendor(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(orderService.getAllItemsForVendor(token));
    }

    @GetMapping("/vendor/orders/pending")
    public ResponseEntity<List<OrderItemDTO>> getPendingOrders(@RequestHeader("Authorization") String token) {
        VendorResponseDTO vendor = vendorClient.getCurrentVendor(token);
        List<OrderItem> items = orderItemRepository
                .findByVendorEmailAndApprovalStatus(vendor.getEmail(), ApprovalStatus.PENDING);
        return ResponseEntity.ok(items.stream().map(OrderItemDTO::new).toList());
    }

    @PutMapping("/vendor/orders/{itemId}/approve")
    public ResponseEntity<String> approveOrderItem(@PathVariable Long itemId,
                                                   @RequestHeader("Authorization") String token) {
        String vendorEmail = jwtUtil.extractUsername(token.substring(7));
        orderService.approveOrderItem(itemId, vendorEmail);
        return ResponseEntity.ok("Order item approved");
    }

    @PutMapping("/vendor/orders/{itemId}/reject")
    public ResponseEntity<String> rejectOrderItem(@PathVariable Long itemId,
                                                  @RequestHeader("Authorization") String token) {
        String vendorEmail = jwtUtil.extractUsername(token.substring(7));
        orderService.rejectOrderItem(itemId, vendorEmail);
        return ResponseEntity.ok("Order item rejected");
    }

    // -------- Delivery Boy APIs (ORDER LEVEL) ---------

    @GetMapping("/delivery/available")
    public ResponseEntity<List<OrderResponse>> getAvailableOrdersForDelivery(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(orderService.getOrdersAvailableForDelivery(token));
    }

    @PutMapping("/delivery/assign/{orderId}")
    public ResponseEntity<String> assignOrderToDeliveryBoy(
            @RequestHeader("Authorization") String token,
            @PathVariable Long orderId) {
        orderService.assignOrderToDeliveryBoy(token, orderId);
        return ResponseEntity.ok("Order assigned to delivery boy");
    }

    @PutMapping("/delivery/update-status/{orderId}")
    public ResponseEntity<String> updateDeliveryStatus(
            @PathVariable Long orderId,
            @RequestParam String status,
            @RequestHeader("Authorization") String token) {
        orderService.updateOrderDeliveryStatus(token, orderId, status);
        return ResponseEntity.ok("Order status updated to: " + status);
    }

    @GetMapping("/my-assigned")
    public List<OrderResponse> myAssigned(@RequestHeader("Authorization") String token) {
        return orderService.getAssignedOrdersForRider(token);
    }

    // -------- Locations (order-level) ---------

    @GetMapping("/{orderId}/deliveryboy-location")
    public Map<String, Object> getAssignedDeliveryBoyLocation(@PathVariable Long orderId) {
        return orderService.getAssignedDeliveryBoyLocation(orderId);
    }

    @GetMapping("/{orderId}/vendor-location")
    public Map<String, Object> getVendorLocation(@PathVariable Long orderId) {
        Order order = orderService.getOrderEntity(orderId);
        if (order.getVendorId() == null) return Map.of("available", false);
        String internalToken = "Bearer " + jwtUtil.generateInternalToken();
        VendorResponseDTO vendor = vendorClient.getVendorById(internalToken, order.getVendorId());
        if (vendor.getLatitude() == null || vendor.getLongitude() == null) return Map.of("available", false);
        return Map.of(
                "available", true,
                "lat", vendor.getLatitude(),
                "lng", vendor.getLongitude(),
                "name", vendor.getCompanyName()
        );
    }

    @GetMapping("/{orderId}/user-location")
    public ResponseEntity<?> getUserLocation(@PathVariable Long orderId,
                                             @RequestHeader("Authorization") String token) {
        Order order = orderService.getOrderEntity(orderId);
        if (order.getAddressId() == null) return ResponseEntity.ok(Map.of("available", false));

        try {
            AddressDTO addr = addressClient.getAddressById(orderService.getInternalToken(), order.getAddressId());
            if (addr == null || addr.getLatitude() == null || addr.getLongitude() == null)
                return ResponseEntity.ok(Map.of("available", false));
            return ResponseEntity.ok(Map.of(
                    "available", true,
                    "lat", addr.getLatitude(),
                    "lng", addr.getLongitude(),
                    "name", "Delivery Address"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("available", false));
        }
    }


    // ⛔️ Legacy item-level delivery endpoints removed
}
