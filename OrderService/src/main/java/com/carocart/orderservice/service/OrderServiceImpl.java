package com.carocart.orderservice.service;

import com.carocart.orderservice.dto.*;
import com.carocart.orderservice.entity.ApprovalStatus;
import com.carocart.orderservice.entity.Order;
import com.carocart.orderservice.entity.OrderItem;
import com.carocart.orderservice.feign.*;
import com.carocart.orderservice.feign.VendorClient;
import com.carocart.orderservice.repository.OrderRepository;
import com.carocart.orderservice.repository.OrderItemRepository;
import com.carocart.orderservice.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

	private String systemJwtToken;

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final ProductClient productClient;
    private final UserClient userClient;
    private final AddressClient addressClient;
    private final NotificationClient notificationClient;
    private final VendorClient vendorClient;
    private final DeliveryBoyClient deliveryBoyClient;
    private final OrderItemRepository orderItemRepository;
    private final OrdersInternalUserClient ordersInternalUserClient;

	@Autowired
    private final JwtUtil jwtUtil;


    public OrderServiceImpl(
    	    OrderRepository orderRepository,
    	    CartClient cartClient,
    	    ProductClient productClient,
    	    UserClient userClient,
    	    AddressClient addressClient,
    	    OrdersInternalUserClient ordersInternalUserClient,
    	    NotificationClient notificationClient,
    	    VendorClient vendorClient,
    	    DeliveryBoyClient deliveryBoyClient,  // <--- ADD THIS PARAM
    	    OrderItemRepository orderItemRepository,
    	    JwtUtil jwtUtil
    	) {
    	    this.orderRepository = orderRepository;
    	    this.cartClient = cartClient;
    	    this.productClient = productClient;
    	    this.userClient = userClient;
    	    this.addressClient = addressClient;
    	    this.ordersInternalUserClient = ordersInternalUserClient;
    	    this.notificationClient = notificationClient;
    	    this.vendorClient = vendorClient;
    	    this.deliveryBoyClient = deliveryBoyClient; // <--- CORRECT ASSIGNMENT
    	    this.orderItemRepository = orderItemRepository;
    	    this.jwtUtil = jwtUtil;
    	}

    @PostConstruct
    public void init() {
        String token = jwtUtil.generateInternalToken();
        this.systemJwtToken = "Bearer " + token;
        System.out.println("✅ Internal token initialized: " + systemJwtToken);
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(String token, OrderRequest orderRequest) {
        // Step 1: Get user and cart
        UserDTO user = userClient.getCurrentUser(token);
        List<CartItemDTO> cartItems = cartClient.getUserCart(token);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty. Cannot place order.");
        }

        // Step 2: Try to get shipping address from AddressService
        AddressDTO address = null;
        try {
            address = addressClient.getAddressById(orderRequest.getAddressId(), token);
            System.out.println("📦 Shipping address: " + address.getAddress());
        } catch (Exception e) {
            System.err.println("⚠ Failed to fetch address details: " + e.getMessage());
        }

        // Step 3: Initialize Order
        Order order = new Order();
        order.setUserId(user.getId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PLACED");

        // ✅ Always store addressId from request
        order.setAddressId(orderRequest.getAddressId());

        // ✅ If AddressService works, use its address, else use provided shippingAddress
        if (address != null && address.getAddress() != null) {
            order.setShippingAddress(address.getAddress());
        } else {
            order.setShippingAddress(orderRequest.getShippingAddress());
        }

     // ✅ Handle payment method
        if ("COD".equalsIgnoreCase(orderRequest.getPaymentMethod())) {
            order.setPaymentStatus("COD");
            order.setPaymentId("COD-" + System.currentTimeMillis());
        } else {
            order.setPaymentStatus("PENDING");
        }

        // Step 4: Fetch vendorId from first product in cart
        try {
            Long firstProductId = cartItems.get(0).getProductId();
            ProductDTO firstProduct = productClient.getProductById(firstProductId);
            if (firstProduct.getVendorId() == null) {
                throw new RuntimeException("Vendor ID is null for product: " + firstProductId);
            }
            order.setVendorId(firstProduct.getVendorId());
            System.out.println("✅ Vendor ID fetched: " + firstProduct.getVendorId());
        } catch (Exception e) {
            System.err.println("❌ Error fetching vendor ID from product: " + e.getMessage());
            throw new RuntimeException("Unable to determine vendor for this order.");
        }

        // Step 5: Build order items
        List<OrderItem> orderItems = cartItems.stream().map(item -> {
            ProductDTO product = productClient.getProductById(item.getProductId());
         // Decrease stock immediately
            try {
                productClient.decreaseStock(product.getId(), item.getQuantity());
            } catch (Exception e) {
                throw new RuntimeException("Failed to decrease stock for product: " + product.getName());
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImageUrl(product.getImageUrl()); // ✅ store image
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setTotalPrice(product.getPrice() * item.getQuantity());
            orderItem.setOrder(order);

            if (product.getVendorEmail() != null) {
                orderItem.setApprovalRequiredBy("VENDOR");
                orderItem.setVendorEmail(product.getVendorEmail());
            } else {
                orderItem.setApprovalRequiredBy("ADMIN");
            }

            orderItem.setApprovalStatus(ApprovalStatus.PENDING);
            return orderItem;
        }).collect(Collectors.toList());

        // Step 6: Finalize order
        double totalAmount = orderItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();
        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        
        double commissionPct = 20.0;
        double commissionAmt = totalAmount * (commissionPct / 100.0);
        order.setPlatformCommissionPct(commissionPct);
        order.setPlatformCommissionAmount(commissionAmt);
        // Step 7: Save order
        Order savedOrder = orderRepository.save(order);

        // Step 8: Clear cart
        cartClient.clearCart(token);
     // ✅ Step 9: Send notifications asynchronously (so they don't block)
        new Thread(() -> {
            try {
                sendOrderPlacedEmails(savedOrder, user, orderItems);
            } catch (Exception e) {
                System.err.println("⚠ Failed to send order emails: " + e.getMessage());
            }
        }).start();
        // Step 10: Send notifications
        sendOrderPlacedEmails(savedOrder, user, orderItems);

        // Step 11: Return response
        return new OrderResponse(savedOrder);
    }


    @Override
    @Transactional
    public OrderResponse placeOrderInternal(OrderRequest orderRequest) {
        List<OrderItemDTO> cartItems = orderRequest.getItems();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty. Cannot place order.");
        }

        // Always use shippingAddress field from OrderRequest for internal API
        String formattedAddress = orderRequest.getShippingAddress() != null
            ? orderRequest.getShippingAddress()
            : "Unknown Address";

        // LOG for debugging
        System.out.println("Internal placeOrder: using shippingAddress = " + formattedAddress);

        Order order = new Order();
        order.setUserId(orderRequest.getUserId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PLACED");
        order.setShippingAddress(formattedAddress);
        order.setPaymentStatus("PAID");
        order.setTotalAmount(orderRequest.getTotalAmount());
     // ✅ VendorId lookup from first product in items
        
        try {
            Long firstProductId = cartItems.get(0).getProductId();
            ProductDTO firstProduct = productClient.getProductById(firstProductId);
            if (firstProduct.getVendorId() != null) {
                order.setVendorId(firstProduct.getVendorId());
            } else {
                throw new RuntimeException("Vendor ID is null for product: " + firstProductId);
            }
        } catch (Exception e) {
            System.err.println("❌ Error fetching vendor ID from product: " + e.getMessage());
            throw new RuntimeException("Unable to determine vendor for this order.");
        }

        // ✅ PaymentId from request (set by PaymentService after Razorpay payment)
        if (orderRequest.getPaymentId() != null && !orderRequest.getPaymentId().isBlank()) {
            order.setPaymentId(orderRequest.getPaymentId());
        }


        List<OrderItem> orderItems = cartItems.stream().map(item -> {
            ProductDTO product = productClient.getProductById(item.getProductId());
         // Decrease stock immediately
            try {
                productClient.decreaseStock(product.getId(), item.getQuantity());
            } catch (Exception e) {
                throw new RuntimeException("Failed to decrease stock for product: " + product.getName());
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(item.getProductId());
//            orderItem.setProductName(item.getProductName() != null ? item.getProductName() : "Product");
            orderItem.setProductName(product.getName());
            orderItem.setProductImageUrl(product.getImageUrl()); // ✅ store image

            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getPrice());
            orderItem.setTotalPrice(item.getPrice() * item.getQuantity());
            orderItem.setOrder(order);
            if (product.getVendorEmail() != null) {
                orderItem.setApprovalRequiredBy("VENDOR");
                orderItem.setVendorEmail(product.getVendorEmail());
            } else {
                orderItem.setApprovalRequiredBy("ADMIN");
            }
            orderItem.setApprovalStatus(ApprovalStatus.PENDING);
            return orderItem;
        }).collect(Collectors.toList());

        order.setOrderItems(orderItems);
     // Apply platform commission (20%)
        double commissionPct = 20.0;
        double commissionAmt = orderRequest.getTotalAmount() * (commissionPct / 100.0);
        order.setPlatformCommissionPct(commissionPct);
        order.setPlatformCommissionAmount(commissionAmt);

        Order savedOrder = orderRepository.save(order);

        return new OrderResponse(savedOrder);
    }

    

    private void sendOrderPlacedEmails(Order order, UserDTO user, List<OrderItem> orderItems) {
        notificationClient.sendEmail(new EmailRequest(
                user.getEmail(),
                "Order Placed Successfully - CaroCart",
                "Hi " + user.getFirstName() + ",\n\nYour order #" + order.getId() + " has been placed successfully."
        ));

        orderItems.stream()
                .map(item -> productClient.getProductById(item.getProductId()))
                .map(ProductDTO::getVendorEmail)
                .filter(email -> email != null && !email.isBlank())
                .distinct()
                .forEach(vendorEmail ->
                        notificationClient.sendEmail(new EmailRequest(
                                vendorEmail,
                                "New Order Received - CaroCart",
                                "You have received a new order #" + order.getId()
                        ))
                );

        notificationClient.sendEmail(new EmailRequest(
                "minnukota381@gmail.com",
                "Order Alert - CaroCart",
                "Order #" + order.getId() + " placed by user " + user.getEmail()
        ));
    }

    private void sendOrderShippedEmails(Order order) {
        // ✅ Use internal token here
        UserDTO user = userClient.getCurrentUserById(systemJwtToken, order.getUserId());

        notificationClient.sendEmail(new EmailRequest(
                user.getEmail(),
                "Order Shipped - CaroCart",
                "Hi " + user.getFirstName() + ",\n\nYour order #" + order.getId() + " has been shipped."
        ));

        order.getOrderItems().stream()
                .map(item -> productClient.getProductById(item.getProductId()))
                .map(ProductDTO::getVendorEmail)
                .filter(email -> email != null && !email.isBlank())
                .distinct()
                .forEach(vendorEmail ->
                        notificationClient.sendEmail(new EmailRequest(
                                vendorEmail,
                                "Order Shipped - CaroCart",
                                "Order #" + order.getId() + " has been shipped."
                        ))
                );

        notificationClient.sendEmail(new EmailRequest(
                "minnukota381@gmail.com",
                "Order Shipped - CaroCart",
                "Order #" + order.getId() + " has been shipped."
        ));
    }

    @Override
    public List<OrderResponse> getOrdersForUser(String token) {
        UserDTO user = userClient.getCurrentUser(token);
        List<OrderResponse> responses = orderRepository.findByUserId(user.getId())
                .stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());

        // 🔹 Fill missing product name/image
        responses.forEach(orderResp -> {
            if (orderResp.getItems() != null) {
                orderResp.getItems().forEach(item -> {
                    if (item.getProductName() == null 
                            || "Product".equals(item.getProductName()) 
                            || item.getProductImageUrl() == null) {
                        try {
                            ProductDTO product = productClient.getProductById(item.getProductId());
                            if (item.getProductName() == null || "Product".equals(item.getProductName())) {
                                item.setProductName(product.getName());
                            }
                            if (item.getProductImageUrl() == null) {
                                item.setProductImageUrl(product.getImageUrl());
                            }
                        } catch (Exception e) {
                            System.err.println("⚠ Could not fetch product for ID " + item.getProductId());
                        }
                    }
                });
            }
        });

        return responses;
    }


    @Override
    public OrderResponse getOrderById(String token, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        try {
            // Use internal token to get user info
            UserDTO user = userClient.getUserById(systemJwtToken, order.getUserId());

            // If the incoming token is not from the same user, and not SYSTEM/ADMIN/DELIVERY_BOY, block it
            String incomingEmail = "";
            try {
                incomingEmail = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            } catch (Exception ignored) {}

            if (!incomingEmail.equalsIgnoreCase(user.getEmail()) &&
                !jwtUtil.extractRole(token.replace("Bearer ", "")).matches("ADMIN|DELIVERY_BOY|SYSTEM")) {
                throw new RuntimeException("Unauthorized access to this order.");
            }

            return new OrderResponse(order);
        } catch (Exception e) {
            throw new RuntimeException("Unable to fetch user info for this order.");
        }
    }


    @Override
    public void cancelOrder(String token, Long orderId) {
        UserDTO user = userClient.getCurrentUser(token);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!order.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized cancellation.");
        }
        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public void cancelOrderByAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatus().equalsIgnoreCase("CANCELLED") ||
                order.getStatus().equalsIgnoreCase("CANCELLED BY ADMIN")) {
            throw new RuntimeException("Order already cancelled.");
        }
        order.setStatus("CANCELLED BY ADMIN");
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::new).collect(Collectors.toList());
    }

    @Override
    public OrderResponse getOrderByIdForAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return new OrderResponse(order);
    }

    @Override
    @Transactional
    public void updatePaymentStatus(Long orderId, String paymentId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setPaymentId(paymentId);
        order.setPaymentStatus(status.toUpperCase());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void markAsCod(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setPaymentId("COD-" + orderId);
        order.setPaymentStatus("COD");
        order.setStatus("PLACED");
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void markOrderAsShipped(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus("SHIPPED");
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        sendOrderShippedEmails(order);
    }
    
    @Override
    public long getTotalOrdersCount() {
        return orderRepository.count();
    }

    public List<OrderItemDTO> getPendingItemsForVendor(String token) {
        VendorResponseDTO vendor = vendorClient.getCurrentVendor(token);
        return orderItemRepository
                .findByVendorEmailAndApprovalStatus(vendor.getEmail(), ApprovalStatus.PENDING) // Use enum, not string!
                .stream()
                .map(OrderItemDTO::new)
                .collect(Collectors.toList());
    }


//    @Override
//    public List<OrderItemDTO> getPendingItemsForAdmin() {
//        return orderItemRepository
//                .findByApprovalRequiredByAndApprovalStatus("ADMIN", "PENDING")
//                .stream()
//                .map(OrderItemDTO::new)
//                .collect(Collectors.toList());
//    }

    @Override
    @Transactional
    public void approveItemAsVendor(String token, Long itemId) {
        VendorResponseDTO vendor = vendorClient.getCurrentVendor(token);
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        if (!vendor.getEmail().equals(item.getVendorEmail())) {
            throw new RuntimeException("Unauthorized");
        }
        item.setApprovalStatus(ApprovalStatus.APPROVED);
        orderItemRepository.save(item);
    }

//    @Override
//    @Transactional
//    public void approveItemAsAdmin(Long itemId) {
//        OrderItem item = orderItemRepository.findById(itemId)
//                .orElseThrow(() -> new RuntimeException("Item not found"));
//        if (!"ADMIN".equals(item.getApprovalRequiredBy())) {
//            throw new RuntimeException("Not an admin item");
//        }
//        item.setApprovalStatus(ApprovalStatus.APPROVED);
//        orderItemRepository.save(item);
//    }

    @Override
    @Transactional
    public void rejectItemAsVendor(String token, Long itemId) {
        VendorResponseDTO vendor = vendorClient.getCurrentVendor(token);
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        if (!vendor.getEmail().equals(item.getVendorEmail())) {
            throw new RuntimeException("Unauthorized");
        }
        item.setApprovalStatus(ApprovalStatus.REJECTED);
        orderItemRepository.save(item);
    }

//    @Override
//    @Transactional
//    public void rejectItemAsAdmin(Long itemId) {
//        OrderItem item = orderItemRepository.findById(itemId)
//                .orElseThrow(() -> new RuntimeException("Item not found"));
//        if (!"ADMIN".equals(item.getApprovalRequiredBy())) {
//            throw new RuntimeException("Not an admin item");
//        }
//        item.setApprovalStatus(ApprovalStatus.REJECTED);
//        
//        orderItemRepository.save(item);
//    }
//    @Override
//    @Transactional
//    public void approveOrderByAdmin(Long orderId) {
//        Order order = orderRepository.findById(orderId)
//            .orElseThrow(() -> new RuntimeException("Order not found"));
//        if ("APPROVED".equalsIgnoreCase(order.getStatus())) {
//            throw new RuntimeException("Order already approved.");
//        }
//        order.setStatus("APPROVED");
//        order.setUpdatedAt(LocalDateTime.now());
//        orderRepository.save(order);
//    }
    
    @Override
    public List<OrderItemDTO> getAllItemsForVendor(String token) {
        VendorResponseDTO vendor = vendorClient.getCurrentVendor(token);
        return orderItemRepository
            .findByVendorEmail(vendor.getEmail())
            .stream()
            .map(OrderItemDTO::new)
            .collect(Collectors.toList());
    }

    public void approveOrderItem(Long itemId, String vendorEmail) {
        OrderItem item = orderItemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Order item not found"));

        if (!item.getVendorEmail().equals(vendorEmail))
            throw new RuntimeException("Unauthorized access");

        item.setApprovalStatus(ApprovalStatus.APPROVED);
        orderItemRepository.save(item);
    }

    public void rejectOrderItem(Long itemId, String vendorEmail) {
        OrderItem item = orderItemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Order item not found"));

        if (!item.getVendorEmail().equals(vendorEmail))
            throw new RuntimeException("Unauthorized access");

        item.setApprovalStatus(ApprovalStatus.REJECTED);
        orderItemRepository.save(item);
    }

    @Override
    public List<OrderResponse> getOrdersAvailableForDelivery(String token) {
        // Fetch only those orders where ALL items are approved by vendor and not assigned
        List<Order> all = orderRepository.findAll();
        return all.stream()
                .filter(order -> order.getDeliveryBoyId() == null &&
                        order.getOrderItems().stream().allMatch(item -> item.getApprovalStatus() == ApprovalStatus.APPROVED))
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<OrderItemDTO> getAvailableOrderItemsForDelivery() {
        List<OrderItem> items = orderItemRepository
                .findByApprovalStatusAndDeliveryBoyIdIsNull(ApprovalStatus.APPROVED);

        return items.stream().map(item -> {
            OrderItemDTO dto = new OrderItemDTO(item);

            Order order = item.getOrder();
            if (order != null) {
                dto.setDeliveryAddress(order.getShippingAddress());
                dto.setPaymentType(order.getPaymentStatus());
                dto.setDeliveryStatus(item.getDeliveryStatus());

                // ✅ Get user details
                try {
                    UserDTO user = userClient.getCurrentUserById(systemJwtToken, order.getUserId());
                    dto.setUserName(user.getFirstName() + " " + user.getLastName());
                    dto.setUserPhone(user.getMobileNumber());
                } catch (Exception e) {
                    System.err.println("⚠️ Could not fetch user details for order " + order.getId() + ": " + e.getMessage());
                    dto.setUserName("N/A");
                    dto.setUserPhone("N/A");
                }

                // ✅ Get user's lat/lng from AddressService
                try {
                    if (order.getAddressId() != null) {
                        AddressDTO address = addressClient.getAddressById(order.getAddressId(), systemJwtToken);
                        if (address != null) {
                            dto.setUserLat(address.getLatitude());
                            dto.setUserLng(address.getLongitude());
                        } else {
                            System.err.println("⚠️ Address not found for ID " + order.getAddressId());
                        }
                    } else {
                        System.err.println("⚠️ Order " + order.getId() + " has no addressId set");
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Could not fetch user location from AddressService for order " + order.getId() + ": " + e.getMessage());
                }
            } else {
                System.err.println("⚠️ OrderItem " + item.getId() + " has no associated Order");
            }

            // ✅ Vendor location
            try {
                ProductDTO product = productClient.getProductById(item.getProductId());
                if (product != null && product.getVendorId() != null) {
                    VendorResponseDTO vendor = vendorClient.getVendorById(systemJwtToken, product.getVendorId());
                    if (vendor != null) {
                        dto.setVendorLat(vendor.getLatitude());
                        dto.setVendorLng(vendor.getLongitude());
                    } else {
                        System.err.println("⚠️ Vendor not found for ID " + product.getVendorId());
                    }
                } else {
                    System.err.println("⚠️ Product vendorId missing for product " + item.getProductId());
                }
            } catch (Exception e) {
                System.err.println("⚠️ Could not fetch vendor location for item " + item.getId() + ": " + e.getMessage());
            }

            return dto;
        }).collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void assignOrderToDeliveryBoy(String token, Long orderId) {
        DeliveryBoyDTO deliveryBoy = deliveryBoyClient.getCurrentDeliveryBoy(token); // create this Feign client if not done

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getDeliveryBoyId() != null)
            throw new RuntimeException("Order already assigned");

        if (order.getOrderItems().stream().anyMatch(item -> item.getApprovalStatus() != ApprovalStatus.APPROVED)) {
            throw new RuntimeException("Some items not approved");
        }

        order.setDeliveryBoyId(deliveryBoy.getId());
        order.setDeliveryStatus("ASSIGNED");
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Notify user and vendor
        sendDeliveryAssignmentEmails(order, deliveryBoy);
    }

    private void sendDeliveryAssignmentEmails(Order order, DeliveryBoyDTO deliveryBoy) {
        UserDTO user = userClient.getCurrentUserById(systemJwtToken, order.getUserId());

        // Notify user
        notificationClient.sendEmail(new EmailRequest(
                user.getEmail(),
                "Order Out for Delivery - CaroCart",
                "Hi " + user.getFirstName() + ",\n\nYour order #" + order.getId() +
                " has been assigned to a delivery boy. You will receive your package shortly."
        ));

        // Notify delivery boy
        notificationClient.sendEmail(new EmailRequest(
                deliveryBoy.getEmail(),
                "Order Assigned to You - CaroCart",
                "Hi " + deliveryBoy.getFirstName() + ",\n\nYou have been assigned order #" + order.getId() +
                ". Please check your delivery dashboard for details."
        ));
    }

    @Override
    public void updateOrderDeliveryStatus(String token, Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // UPDATE deliveryStatus in all items of this order
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            item.setDeliveryStatus(status);  // <- Ensure this is synced
        }
        orderItemRepository.saveAll(items);
    }


    private void sendStatusUpdateNotifications(Order order, String status) {
        UserDTO user = userClient.getCurrentUserById(systemJwtToken, order.getUserId());

        String subject = "Order #" + order.getId() + " Status Update - CaroCart";
        String userMsg = "Hi " + user.getFirstName() + ",\n\nYour order status is now: " + status + ".";
        String vendorMsg = "Order #" + order.getId() + " is now: " + status;

        // Notify user
        notificationClient.sendEmail(new EmailRequest(user.getEmail(), subject, userMsg));

        // Notify vendor(s)
        order.getOrderItems().stream()
            .map(OrderItem::getVendorEmail)
            .filter(email -> email != null && !email.isBlank())
            .distinct()
            .forEach(vendorEmail ->
                    notificationClient.sendEmail(new EmailRequest(vendorEmail, subject, vendorMsg))
            );

        // Optionally notify admin
        notificationClient.sendEmail(new EmailRequest(
            "minnukota381@gmail.com", subject, "Order #" + order.getId() + " updated to " + status
        ));
    }

    @Override
    @Transactional
    public void assignOrderItemToDeliveryBoy(Long orderItemId, Long deliveryBoyId) {
        OrderItem item = orderItemRepository.findById(orderItemId)
            .orElseThrow(() -> new RuntimeException("OrderItem not found"));
        if (item.getDeliveryBoyId() != null)
            throw new RuntimeException("Already assigned");
        item.setDeliveryBoyId(deliveryBoyId);
        item.setDeliveryStatus("ASSIGNED");
        orderItemRepository.save(item);
    }
    @Override
    @Transactional
    public void updateOrderItemDeliveryStatus(Long orderItemId, String status) {
        OrderItem item = orderItemRepository.findById(orderItemId)
            .orElseThrow(() -> new RuntimeException("OrderItem not found"));

        item.setDeliveryStatus(status);
        orderItemRepository.save(item);

        Long orderId = item.getOrder().getId();
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        boolean allDelivered = items.stream()
            .allMatch(i -> "DELIVERED".equalsIgnoreCase(i.getDeliveryStatus()));

        boolean allOutForDelivery = items.stream()
            .allMatch(i -> "OUT_FOR_DELIVERY".equalsIgnoreCase(i.getDeliveryStatus()));

        Order order = item.getOrder();

        // ✅ Set order status
        if (allDelivered) {
            order.setStatus("DELIVERED");
        } else if (allOutForDelivery) {
            order.setStatus("OUT_FOR_DELIVERY");
        }

        // ✅ Ensure delivery fields are updated too
        if (order.getDeliveryBoyId() == null && item.getDeliveryBoyId() != null) {
            order.setDeliveryBoyId(item.getDeliveryBoyId());
        }

        order.setDeliveryStatus(status); // ✅ Sync main deliveryStatus

        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }



    @Override
    public Order getOrderEntity(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }


    @Override
    public Map<String, Object> getAssignedDeliveryBoyLocation(Long orderId) {
    	System.out.println("🚨 internalToken being used: " + systemJwtToken);

        Order order = getOrderEntity(orderId);
        if (order.getDeliveryBoyId() == null) return Map.of("available", false);
        
        // Proper internal token usage
        DeliveryBoyDTO boy = deliveryBoyClient.getById(systemJwtToken, order.getDeliveryBoyId());

        return Map.of(
            "available", true,
            "lat", boy.getCurrentLat(),
            "lng", boy.getCurrentLng(),
            "name", boy.getFirstName() + " " + boy.getLastName(),
            "phone", boy.getPhone()
        );
    }
    
    @Override
    public String getInternalToken() {
        return this.systemJwtToken;
    }
    @Override
    public List<VendorEarningsDTO> getVendorEarnings(String fromDate, String toDate) {
        LocalDateTime from = LocalDateTime.parse(fromDate + "T00:00:00");
        LocalDateTime to = LocalDateTime.parse(toDate + "T23:59:59");

        // Fetch delivered orders in range
        List<Order> orders = orderRepository.findByStatusAndOrderDateBetween("DELIVERED", from, to);

        // Group by vendor
        Map<Long, List<Order>> grouped = orders.stream()
                .collect(Collectors.groupingBy(Order::getVendorId));

        return grouped.entrySet().stream().map(entry -> {
            Long vendorId = entry.getKey();
            List<Order> vendorOrders = entry.getValue();

            double totalSales = vendorOrders.stream().mapToDouble(Order::getTotalAmount).sum();
            double commission = vendorOrders.stream().mapToDouble(Order::getPlatformCommissionAmount).sum();

            // Fetch vendor info via internal token
            VendorResponseDTO vendor = vendorClient.getVendorById(systemJwtToken, vendorId);

            return new VendorEarningsDTO(
                    vendorId,
                    vendor.getCompanyName(),
                    vendor.getUpiId(),
                    totalSales,
                    commission
            );
        }).collect(Collectors.toList());
    }

}