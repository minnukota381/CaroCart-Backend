package com.carocart.orderservice.controller;

import com.carocart.orderservice.dto.*;
import com.carocart.orderservice.entity.ApprovalStatus;
import com.carocart.orderservice.entity.Order;
import com.carocart.orderservice.entity.OrderItem;
import com.carocart.orderservice.feign.DeliveryBoyClient;
import com.carocart.orderservice.feign.UserClient;
import com.carocart.orderservice.feign.VendorClient;
import com.carocart.orderservice.repository.OrderItemRepository;
import com.carocart.orderservice.service.OrderService;
import com.carocart.orderservice.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderItemRepository orderItemRepository;
    private final VendorClient vendorClient;
    private final JwtUtil jwtUtil;
    private final DeliveryBoyClient deliveryBoyClient;
    private final UserClient userClient;
    @Value("${jwt.secret}")
    private String jwtSecret;

    private String internalToken;
    @Autowired
    public OrderController(OrderService orderService,
                           OrderItemRepository orderItemRepository,
                           VendorClient vendorClient,
                           DeliveryBoyClient deliveryBoyClient,
                           JwtUtil jwtUtil,UserClient userClient) {
        this.orderService = orderService;
        this.orderItemRepository = orderItemRepository;
        this.vendorClient = vendorClient;
        this.deliveryBoyClient = deliveryBoyClient;
        this.jwtUtil = jwtUtil;
		this.userClient = userClient;
    }

    // ----------------- USER APIs -----------------

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
        OrderResponse response = orderService.placeOrderInternal(orderRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/internal/place")
    public ResponseEntity<OrderResponse> placeOrderInternal(@RequestBody OrderRequest orderRequest) {
        OrderResponse response = orderService.placeOrderInternal(orderRequest);
        return ResponseEntity.ok(response);
    }

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
        List<OrderItem> items = orderItemRepository.findByVendorEmailAndApprovalStatus(vendor.getEmail(), ApprovalStatus.PENDING);
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

    // -------- Delivery Boy APIs ---------

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

    @GetMapping("/orderitems/available-for-delivery")
    public List<OrderItemDTO> getAvailableOrderItemsForDelivery() {
        return orderService.getAvailableOrderItemsForDelivery();
    }

    @GetMapping("/orderitems/assigned/{deliveryBoyId}")
    public List<OrderItemDTO> getAssignedOrderItems(@PathVariable Long deliveryBoyId) {
        List<OrderItem> items = orderItemRepository.findByDeliveryBoyId(deliveryBoyId);
        return items.stream().map(OrderItemDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/orderitems/delivered/{deliveryBoyId}")
    public List<OrderItemDTO> getDeliveredOrderItems(@PathVariable Long deliveryBoyId) {
        List<OrderItem> items = orderItemRepository.findByDeliveryBoyIdAndDeliveryStatus(deliveryBoyId, "DELIVERED");
        return items.stream().map(OrderItemDTO::new).collect(Collectors.toList());
    }

    @PutMapping("/orderitems/{orderItemId}/assign-delivery")
    public ResponseEntity<String> assignOrderItemToDeliveryBoy(
            @PathVariable Long orderItemId,
            @RequestParam Long deliveryBoyId) {
        orderService.assignOrderItemToDeliveryBoy(orderItemId, deliveryBoyId);
        return ResponseEntity.ok("Order item assigned to delivery boy");
    }

    @PutMapping("/orderitems/{orderItemId}/delivery-status")
    public ResponseEntity<String> updateOrderItemDeliveryStatus(
            @PathVariable Long orderItemId,
            @RequestParam String status) {
        orderService.updateOrderItemDeliveryStatus(orderItemId, status);
        return ResponseEntity.ok("Order item delivery status updated to: " + status);
    }

    @GetMapping("/{orderId}/deliveryboy-location")
    public Map<String, Object> getAssignedDeliveryBoyLocation(@PathVariable Long orderId) {
        Order order = orderService.getOrderEntity(orderId);
        if (order.getDeliveryBoyId() == null)
            return Map.of("available", false);

        // ✅ Get token from OrderServiceImpl
        String token = orderService.getInternalToken();
        System.out.println("Token being sent to DeliveryService: " + token);

        DeliveryBoyDTO boy = deliveryBoyClient.getById(token, order.getDeliveryBoyId());
        return Map.of(
                "available", true,
                "lat", boy.getCurrentLat(),
                "lng", boy.getCurrentLng(),
                "name", boy.getFirstName() + " " + boy.getLastName(),
                "phone", boy.getPhone()
        );
    }
    
    @GetMapping("/{orderId}/vendor-location")
    public Map<String, Object> getVendorLocation(@PathVariable Long orderId) {
        Order order = orderService.getOrderEntity(orderId);
        if (order.getVendorId() == null)
            return Map.of("available", false);

        // ✅ FIX: Generate internal token
        String internalToken = "Bearer " + jwtUtil.generateInternalToken();
        // ✅ Pass internal token
        VendorResponseDTO vendor = vendorClient.getVendorById(internalToken, order.getVendorId());

        if (vendor.getLatitude() == null || vendor.getLongitude() == null)
            return Map.of("available", false);

        return Map.of(
            "available", true,
            "lat", vendor.getLatitude(),
            "lng", vendor.getLongitude(),
            "name", vendor.getCompanyName()
        );
    }


    // ✅ NEW: User Location
    @GetMapping("/{orderId}/user-location")
    public ResponseEntity<?> getUserLocation(@PathVariable Long orderId) {
        Order order = orderService.getOrderEntity(orderId);
        if (order.getUserId() == null) {
            return ResponseEntity.ok(Map.of("available", false));
        }

        String internalToken = "Bearer " + jwtUtil.generateInternalToken(); // ✅ Use internal token
        UserDTO user = userClient.getUserById(internalToken, order.getUserId()); // ✅ Pass it here

        return ResponseEntity.ok(Map.of(
            "available", true,
            "lat", user.getLatitude(),
            "lng", user.getLongitude(),
            "name", user.getFirstName()
        ));
    }



}
