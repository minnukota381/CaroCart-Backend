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
import java.util.stream.Collectors;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.text.NumberFormat;
import java.util.Locale;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Optional;

import com.carocart.orderservice.entity.VendorSettlementStatus;


@Service
public class OrderServiceImpl implements OrderService {

    @Value("${orders.autoApprove.enabled:true}")
    private boolean autoApproveEnabled;

    @Value("${orders.autoApprove.delaySeconds:60}")
    private long autoApproveDelaySeconds;
    
	private String systemJwtToken;

	// default-zero helpers
	private static double dz(Double v) { return v == null ? 0.0 : v; }
	private static double bz(java.math.BigDecimal v) { return v == null ? 0.0 : v.doubleValue(); }

	public boolean isFirstOrder(Long userId) {
	    return orderRepository.countByUserId(userId) == 0;
	}

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final ProductClient productClient;
    private final UserClient userClient;
    private final AddressClient addressClient;
    private final PaymentClient paymentClient;
    private final NotificationClient notificationClient;
    private final VendorClient vendorClient;
    private final DeliveryBoyClient deliveryBoyClient;
    private final OrderItemRepository orderItemRepository;
    private final OrdersInternalUserClient ordersInternalUserClient;
 // fields
    private final DeliveryEarningClient deliveryEarningClient;

	@Autowired
    private final JwtUtil jwtUtil;
	// at top of OrderServiceImpl
	private static final LocalDateTime SQL_MIN = LocalDateTime.of(1970, 1, 1, 0, 0);


    public OrderServiceImpl(
    	    OrderRepository orderRepository,
    	    CartClient cartClient,
    	    ProductClient productClient,
    	    UserClient userClient,
    	    AddressClient addressClient,
    	    PaymentClient paymentClient,
    	    OrdersInternalUserClient ordersInternalUserClient,
    	    NotificationClient notificationClient,
    	    VendorClient vendorClient,
    	    DeliveryBoyClient deliveryBoyClient,  // <--- ADD THIS PARAM
    	    OrderItemRepository orderItemRepository,
    	    JwtUtil jwtUtil,
    	    DeliveryEarningClient deliveryEarningClient
    	    

    	) {
    	    this.orderRepository = orderRepository;
    	    this.cartClient = cartClient;
    	    this.productClient = productClient;
    	    this.userClient = userClient;
    	    this.addressClient = addressClient;
    	    this.paymentClient = paymentClient;
    	    this.ordersInternalUserClient = ordersInternalUserClient;
    	    this.notificationClient = notificationClient;
    	    this.vendorClient = vendorClient;
    	    this.deliveryBoyClient = deliveryBoyClient; // <--- CORRECT ASSIGNMENT
    	    this.orderItemRepository = orderItemRepository;
    	    this.jwtUtil = jwtUtil;
    	    this.deliveryEarningClient = deliveryEarningClient;
    	}

 // ====== Delivery pricing config with safe defaults ======
    @Value("${delivery.threshold.grocery:349}")
    private double FREE_GROCERY;

    @Value("${delivery.threshold.food:399}")
    private double FREE_FOOD;

    // If you want to fetch slabs from config later, you can.
    // For now, keep the logic centralized here with defaults.
    private double feeFromSlab(double km) {
        if (km <= 1)  return 0.0;  // Free ≤1 km
        if (km <= 2)  return 15.0;
        if (km <= 3)  return 19.0;
        if (km <  6)  return 29.0;
        if (km <  9)  return 39.0;
        if (km < 12)  return 59.0;
        throw new RuntimeException("Delivery not available for this distance.");
    }

    private boolean qualifiesForFree(double cartAmount, boolean isGrocery) {
        return cartAmount >= (isGrocery ? FREE_GROCERY : FREE_FOOD);
    }

    /** Always the slab payout for the rider, regardless of what the customer pays */
    private double riderPayoutByDistance(double km) {
        return feeFromSlab(km);
    }

    /** Compute vendor→user distance for an ORDER */
    private double distanceKmForOrder(Long vendorId, Long addressId) {
        VendorResponseDTO vendor = vendorClient.getVendorById(systemJwtToken, vendorId);
        AddressDTO address = addressClient.getAddressById(systemJwtToken, addressId);

        if (vendor.getLatitude() == null || vendor.getLongitude() == null
                || address.getLatitude() == null || address.getLongitude() == null) {
            throw new RuntimeException("Cannot compute distance: vendor or address lat/lng missing.");
        }
        return haversine(vendor.getLatitude(), vendor.getLongitude(),
                         address.getLatitude(), address.getLongitude());
    }

    /** Food/Grocery */
    private boolean isGroceryVendor(Long vendorId) {
        VendorResponseDTO vendor = vendorClient.getVendorById(systemJwtToken, vendorId);
        return vendor.getVendorType() != null
                && vendor.getVendorType().name().equalsIgnoreCase("GROCERY");
    }

 // --- Haversine formula for distance in KM ---
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
    	
        final int R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // --- Delivery Fee Logic ---
    private double calculateDeliveryFee(double distanceKm, double cartAmount, boolean isGrocery) {
        double freeThreshold = isGrocery ? 149.0 : 199.0;
        if (cartAmount >= freeThreshold) return 0.0;

        if (distanceKm <= 3) return 19.0;
        if (distanceKm <= 6) return 29.0;
        if (distanceKm <= 9) return 39.0;
        if (distanceKm <= 12 && !isGrocery) return 59.0;

        throw new RuntimeException("❌ Delivery not available for this distance.");
    }

    
    @PostConstruct
    public void init() {
        String token = jwtUtil.generateInternalToken();
        this.systemJwtToken = "Bearer " + token;
        System.out.println("✅ Internal token initialized: " + systemJwtToken);
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(String token, OrderRequest orderRequest, String couponCode) {
        orderRequest.setCouponCode(couponCode);

        // 1) user + cart
        UserDTO user = userClient.getCurrentUser(token);
        List<CartItemDTO> cartItems = cartClient.getUserCart(token);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty. Cannot place order.");
        }

        // 2) address
        AddressDTO address = addressClient.getAddressById(token, orderRequest.getAddressId());

        // 3) order shell
        Order order = new Order();
        order.setUserId(user.getId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PLACED");
        order.setAddressId(orderRequest.getAddressId());
        order.setShippingAddress(address.getAddress());
        order.setPaymentMethod(
            (orderRequest.getPaymentMethod() != null) ? orderRequest.getPaymentMethod() : "COD"
        );
        if ("COD".equalsIgnoreCase(order.getPaymentMethod())) {
            order.setPaymentStatus("COD");
            order.setPaymentId("COD-" + System.currentTimeMillis());
        } else {
            order.setPaymentStatus("PENDING");
        }

        // 4) vendor
        Long firstProductId = cartItems.get(0).getProductId();
        ProductDTO firstProduct = productClient.getProductById(firstProductId);
        if (firstProduct.getVendorId() == null) {
            throw new RuntimeException("Vendor ID is null for product: " + firstProductId);
        }
        order.setVendorId(firstProduct.getVendorId());
        vendorClient.getVendorById(systemJwtToken, firstProduct.getVendorId());

        // 5) items (authoritative pricing)
        List<OrderItem> orderItems = cartItems.stream().map(item -> {
            ProductDTO product = productClient.getProductById(item.getProductId());
            productClient.decreaseStock(product.getId(), item.getQuantity());

            OrderItem oi = new OrderItem();
            oi.setProductId(product.getId());
            oi.setProductName(product.getName());
            oi.setProductImageUrl(product.getImageUrl());
            oi.setQuantity(item.getQuantity());
            oi.setPrice(product.getPrice());
            oi.setTotalPrice(product.getPrice() * item.getQuantity());
            oi.setOrder(order);
            oi.setApprovalRequiredBy(product.getVendorEmail() != null ? "VENDOR" : "ADMIN");
            oi.setVendorEmail(product.getVendorEmail());
            oi.setApprovalStatus(ApprovalStatus.PENDING);
            return oi;
        }).collect(Collectors.toList());

        // 6) totals
        double itemsTotal = orderItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();

        // 🔒 Coupon validation: only FIRST20 allowed, and only on first real order
        double discount = 0.0;
        String c = (orderRequest.getCouponCode() == null) ? null : orderRequest.getCouponCode().trim();

        if (c != null && !c.isEmpty()) {
            if (!c.equalsIgnoreCase("FIRST20")) {
                throw new IllegalArgumentException("Invalid coupon code.");
            }
            long priorRealOrders = orderRepository.countByUserIdAndStatusIn(
                order.getUserId(),
                List.of("PLACED", "PAID", "COMPLETED", "DELIVERED")
            );
            if (priorRealOrders == 0) {
            	discount = itemsTotal * 0.15;
            	order.setCouponApplied("FIRST15");

            } else {
                throw new IllegalArgumentException("FIRST20 is valid only on your first order.");
            }
            order.setDiscountAmount(discount);
            order.setCouponCode(c);
        }

        double discountedTotal = itemsTotal - discount;

        order.setOrderItems(orderItems);
        order.setTotalAmount(itemsTotal);
        order.setPlatformCommissionPct(20.0);
        order.setPlatformCommissionAmount(itemsTotal * 0.20);

        // delivery fee
        double distanceKm = distanceKmForOrder(order.getVendorId(), order.getAddressId());
        boolean isGrocery = isGroceryVendor(order.getVendorId());
        double customerFee = feeFromSlab(distanceKm);
        if (qualifiesForFree(discountedTotal, isGrocery)) customerFee = 0.0;
        order.setDeliveryFee(customerFee);

        order.setFinalAmount(discountedTotal + customerFee);

        // 7) save
        Order savedOrder = orderRepository.save(order);

        // 8) clear cart
        cartClient.clearCart(token);

        // 9) notify
        new Thread(() -> sendOrderPlacedEmails(savedOrder, user, orderItems)).start();

        return new OrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse placeOrderInternal(OrderRequest orderRequest, String couponCode) {
        List<OrderItemDTO> cartItems = orderRequest.getItems();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty. Cannot place order.");
        }

        Order order = new Order();
        order.setUserId(orderRequest.getUserId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PLACED");
        order.setAddressId(orderRequest.getAddressId());
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setPaymentStatus("PAID");
        order.setPaymentMethod(
            (orderRequest.getPaymentMethod() != null) ? orderRequest.getPaymentMethod() : "RAZORPAY"
        );

        Long firstProductId = cartItems.get(0).getProductId();
        ProductDTO firstProduct = productClient.getProductById(firstProductId);
        if (firstProduct.getVendorId() == null) {
            throw new RuntimeException("Vendor ID is null for product: " + firstProductId);
        }
        order.setVendorId(firstProduct.getVendorId());
        vendorClient.getVendorById(systemJwtToken, firstProduct.getVendorId());
        AddressDTO address = addressClient.getAddressById(systemJwtToken, orderRequest.getAddressId());

        // items
        List<OrderItem> orderItems = cartItems.stream().map(item -> {
            ProductDTO product = productClient.getProductById(item.getProductId());
            productClient.decreaseStock(product.getId(), item.getQuantity());

            OrderItem oi = new OrderItem();
            oi.setProductId(product.getId());
            oi.setProductName(product.getName());
            oi.setProductImageUrl(product.getImageUrl());
            oi.setQuantity(item.getQuantity());
            oi.setPrice(product.getPrice());
            oi.setTotalPrice(product.getPrice() * item.getQuantity());
            oi.setOrder(order);
            oi.setApprovalRequiredBy(product.getVendorEmail() != null ? "VENDOR" : "ADMIN");
            oi.setVendorEmail(product.getVendorEmail());
            oi.setApprovalStatus(ApprovalStatus.PENDING);
            return oi;
        }).collect(Collectors.toList());

        double itemsTotal = orderItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();

        // 🔒 Coupon validation: only FIRST20 allowed, and only on first real order
        double discount = 0.0;
        String c = (couponCode == null) ? null : couponCode.trim();

        if (c != null && !c.isEmpty()) {
            if (!c.equalsIgnoreCase("FIRST20")) {
                throw new IllegalArgumentException("Invalid coupon code.");
            }
            long priorRealOrders = orderRepository.countByUserIdAndStatusIn(
                orderRequest.getUserId(),
                List.of("PLACED", "PAID", "COMPLETED", "DELIVERED")
            );
            if (priorRealOrders == 0) {
            	discount = itemsTotal * 0.15;
            	order.setCouponApplied("FIRST15");

            } else {
                throw new IllegalArgumentException("FIRST20 is valid only on your first order.");
            }
            order.setDiscountAmount(discount);
            order.setCouponCode(c);
        }

        double discountedTotal = itemsTotal - discount;

        order.setOrderItems(orderItems);
        order.setTotalAmount(itemsTotal);
        order.setPlatformCommissionPct(20.0);
        order.setPlatformCommissionAmount(itemsTotal * 0.20);

        // delivery fee
        double distanceKm = distanceKmForOrder(order.getVendorId(), order.getAddressId());
        boolean isGrocery = isGroceryVendor(order.getVendorId());
        double customerFee = feeFromSlab(distanceKm);
        if (qualifiesForFree(discountedTotal, isGrocery)) customerFee = 0.0;
        order.setDeliveryFee(customerFee);

        order.setFinalAmount(discountedTotal + customerFee);

        Order saved = orderRepository.save(order);

        try {
            UserDTO user = userClient.getUserById(systemJwtToken, order.getUserId());
            new Thread(() -> sendOrderPlacedEmails(saved, user, orderItems)).start();
        } catch (Exception e) {
            System.err.println("⚠ Failed to send Razorpay order-placed emails for order "
                + saved.getId() + ": " + e.getMessage());
        }

        return new OrderResponse(saved);
    }



    private void sendOrderPlacedEmails(Order order, UserDTO user, List<OrderItem> orderItems) {
        String fe = System.getenv().getOrDefault("http://localhost:5173","https://carocart.com");

        int randomRef = (int)(Math.random() * 900000) + 100000; // 6-digit ref no

        String orderLinkUser  = "%s/user/orders/%d".formatted(fe, order.getId());
        String orderLinkAdmin = "%s/admin/orders/%d".formatted(fe, order.getId());

        String userHtml = buildOrderHtml(
            order,
            "Order placed successfully",
            "Thanks, "+safe(user.getFirstName())+"! We’re getting your order ready.",
            orderItems,
            orderLinkUser,
            "Track order"
        );
        notificationClient.sendEmail(new EmailRequest(
            user.getEmail(), "Order Placed ✔ – #" + randomRef, userHtml, true
        ));

        var byVendor = orderItems.stream()
            .filter(it -> productClient.getProductById(it.getProductId()).getVendorEmail()!=null)
            .collect(java.util.stream.Collectors.groupingBy(
                it -> productClient.getProductById(it.getProductId()).getVendorEmail()));

        for (var e : byVendor.entrySet()) {
            String vhtml = buildOrderHtml(
                order, "New order received", "Please prepare the following items.",
                e.getValue(), "%s/vendor/orders/%d".formatted(fe, order.getId()), "Open order");
            notificationClient.sendEmail(new EmailRequest(e.getKey(),
                "New Order – #"+randomRef, vhtml, true));
        }

        String adminHtml = buildOrderHtml(
            order, "New order created", "Real-time alert for admin dashboard.",
            orderItems, orderLinkAdmin, "Open in Admin");
        notificationClient.sendEmail(new EmailRequest(
            "carocartteam@gmail.com", "Order Alert – #"+randomRef, adminHtml, true));
    }


    private void sendOrderShippedEmails(Order order) {
        UserDTO user = userClient.getCurrentUserById(systemJwtToken, order.getUserId());
        String link = "%s/user/orders/%d".formatted(System.getenv().getOrDefault("http://localhost:5173","https://www.carocart.com"), order.getId());
        String html = buildOrderHtml(order, "Your order is on the way 🚚", "We’ve shipped your order.", order.getOrderItems(), link, "Track order");
        notificationClient.sendEmail(new EmailRequest(user.getEmail(), "Shipped – #"+order.getId(), html, true));

        // Vendors
        Map<String, List<OrderItem>> byVendor = order.getOrderItems().stream()
            .filter(it -> productClient.getProductById(it.getProductId()).getVendorEmail()!=null)
            .collect(Collectors.groupingBy(it -> productClient.getProductById(it.getProductId()).getVendorEmail()));
        for (var e: byVendor.entrySet()) {
            String vhtml = buildOrderHtml(order, "Order shipped", "FYI – customer’s order is shipped.", e.getValue(),
                    "%s/vendor/orders/%d".formatted(System.getenv().getOrDefault("http://localhost:5173","https://www.carocart.com"), order.getId()),
                    "Open order");
            notificationClient.sendEmail(new EmailRequest(e.getKey(), "Order Shipped – #"+order.getId(), vhtml, true));
        }

        // Admin
        String ahtml = buildOrderHtml(order, "Order shipped", "Status moved to SHIPPED.", order.getOrderItems(),
                "%s/admin/orders/%d".formatted(System.getenv().getOrDefault("http://localhost:5173","https://www.carocart.com"), order.getId()),
                "Open in Admin");
        notificationClient.sendEmail(new EmailRequest("carocartteam@gmail.com", "Order Shipped – #"+order.getId(), ahtml, true));
    }

    @Override
    public List<OrderResponse> getOrdersForUser(String token) {
        UserDTO user = userClient.getCurrentUser(token);
        List<OrderResponse> responses = orderRepository.findByUserId(user.getId())
                .stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());

        // Fill missing product info
        responses.forEach(orderResp -> {
            if (orderResp.getItems() != null) {
                orderResp.getItems().forEach(item -> {
                    try {
                        ProductDTO product = productClient.getProductById(item.getProductId());
                        item.setProductName(product.getName());
                        item.setProductImageUrl(product.getImageUrl());
                    } catch (Exception e) {
                        System.err.println("⚠ Could not fetch product for ID " + item.getProductId());
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
            UserDTO user = userClient.getUserById(systemJwtToken, order.getUserId());
            String incomingEmail = "";
            try { incomingEmail = jwtUtil.extractUsername(token.replace("Bearer ", "")); } catch (Exception ignored) {}
            if (!incomingEmail.equalsIgnoreCase(user.getEmail()) &&
                !jwtUtil.extractRole(token.replace("Bearer ", "")).matches("ADMIN|DELIVERY_BOY|SYSTEM")) {
                throw new RuntimeException("Unauthorized access to this order.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to fetch user info for this order.");
        }

        return toEnrichedResponse(order);
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
                .map(this::toEnrichedResponse)   // <— was OrderResponse::new
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public OrderResponse getOrderByIdForAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return toEnrichedResponse(order);        // <— was new OrderResponse(order)
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

    @Override
    public List<OrderItemDTO> getPendingItemsForAdmin() {
        // Admin can see all pending items
        return orderItemRepository.findByApprovalStatus(ApprovalStatus.PENDING)
                .stream()
                .map(OrderItemDTO::new)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public void approveOrderByAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Approve all pending items under this order
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        boolean changed = false;
        for (OrderItem it : items) {
            if (it.getApprovalStatus() != ApprovalStatus.APPROVED) {
                it.setApprovalStatus(ApprovalStatus.APPROVED);
                changed = true;
            }
        }
        if (changed) {
            orderItemRepository.saveAll(items);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }
        // Optional: send notifications here if you want
    }

    @Override
    @Transactional
    public void rejectOrderByAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Reject all non-rejected items under this order
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        boolean changed = false;
        for (OrderItem it : items) {
            if (it.getApprovalStatus() != ApprovalStatus.REJECTED) {
                it.setApprovalStatus(ApprovalStatus.REJECTED);
                changed = true;
            }
        }
        if (changed) {
            orderItemRepository.saveAll(items);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }
        // Note: You already have a separate "cancel by admin" if you want to also cancel the order.
    }

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
    @Transactional
    public void assignOrderToDeliveryBoy(String token, Long orderId) {
        DeliveryBoyDTO rider = deliveryBoyClient.getCurrentDeliveryBoy(token);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getDeliveryBoyId() != null) {
            throw new RuntimeException("Order already assigned");
        }
        if (order.getOrderItems().stream().anyMatch(i -> i.getApprovalStatus() != ApprovalStatus.APPROVED)) {
            throw new RuntimeException("Some items not approved");
        }

        // Set on order + all items
        order.setDeliveryBoyId(rider.getId());
        order.setDeliveryStatus("ASSIGNED");
        if (order.getStatus() == null || !"CANCELLED".equalsIgnoreCase(order.getStatus())) {
            // keep business status aligned with delivery phases if you wish
            order.setStatus("PLACED"); // or keep existing
        }
        order.getOrderItems().forEach(i -> {
            i.setDeliveryBoyId(rider.getId());
            i.setDeliveryStatus("ASSIGNED");
        });

        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order); // CascadeType.ALL will persist item changes too

    }

    @Override
    @Transactional
    public void updateOrderDeliveryStatus(String token, Long orderId, String status) {
        String s = status == null ? "" : status.trim().toUpperCase();

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        // Propagate delivery status to order + items
        order.setDeliveryStatus(s);
        order.getOrderItems().forEach(i -> i.setDeliveryStatus(s));
        order.setUpdatedAt(LocalDateTime.now());

        // Optionally mirror delivery phases to business status
        switch (s) {
            case "ASSIGNED" -> order.setStatus("PROCESSING");
            case "PICKED_UP" -> order.setStatus("PREPARING");
            case "OUT_FOR_DELIVERY" -> order.setStatus("OUT_FOR_DELIVERY");
            case "DELIVERED" -> {
                order.setStatus("DELIVERED");
               // ✅ mark the moment it became payout-eligible
              order.setDeliveredAt(LocalDateTime.now());
               // keep/ensure vendor settlement flag is UNPAID until payout runs
               if (order.getVendorSettlementStatus() == null) {
                   order.setVendorSettlementStatus(VendorSettlementStatus.UNPAID);
               }
            }
            default -> { /* no-op */ }
        }

        orderRepository.save(order);

        // Also persist via item repo (idempotent; helpful if items were detached)
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        items.forEach(i -> i.setDeliveryStatus(s));
        orderItemRepository.saveAll(items);

        // Create rider earning once when the WHOLE order is delivered
        if ("DELIVERED".equalsIgnoreCase(s)) {
            try {
                // Use a stable reference itemId for idempotency
                long referenceItemId = items.stream()
                    .mapToLong(OrderItem::getId)
                    .min()
                    .orElseThrow(() -> new RuntimeException("Order has no items"));

                // Compute payout by distance (independent of what customer paid)
                double distanceKm = distanceKmForOrder(order.getVendorId(), order.getAddressId());
                double payout = riderPayoutByDistance(distanceKm);

                Long boyId = order.getDeliveryBoyId();
                if (boyId == null) {
                    // Fallback to any item’s deliveryBoyId if order-level is missing
                    boyId = items.stream().map(OrderItem::getDeliveryBoyId).filter(id -> id != null).findFirst().orElse(null);
                }

                if (boyId == null) {
                    System.err.println("⚠️ Skipping earning creation: no deliveryBoyId set.");
                } else {
                    var req = new com.carocart.orderservice.dto.DeliveryEarningCreateRequest(
                        referenceItemId, // used by Delivery service to de-duplicate
                        boyId,
                        payout,
                        0.0,
                        0.0,
                        LocalDateTime.now()
                    );
                    deliveryEarningClient.create(systemJwtToken, req);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Failed to create delivery earning for order " + order.getId() + ": " + e.getMessage());
            }
        }

        // Optional: notify user/vendor(s)
        // sendStatusUpdateNotifications(order, s);
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
            "carocartteam@gmail.com", subject, "Order #" + order.getId() + " updated to " + status
        ));
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
    public List<VendorEarningsDTO> getVendorEarningsByPeriod(String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from;
        LocalDateTime to = now;

        switch (period.toUpperCase()) {
            case "DAILY":
                from = now.toLocalDate().atStartOfDay();
                to = now.toLocalDate().atTime(23, 59, 59);
                break;
            case "WEEKLY":
                from = now.minusDays(7).toLocalDate().atStartOfDay();
                break;
            case "MONTHLY":
                from = now.minusDays(30).toLocalDate().atStartOfDay();
                break;
            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }

        List<Order> orders = orderRepository.findByStatusAndOrderDateBetween("DELIVERED", from, to);

        Map<Long, List<Order>> grouped = orders.stream()
                .collect(Collectors.groupingBy(Order::getVendorId));

        return grouped.entrySet().stream().map(entry -> {
            Long vendorId = entry.getKey();
            List<Order> vendorOrders = entry.getValue();

            double totalSales = vendorOrders.stream().mapToDouble(Order::getTotalAmount).sum();
            double commission = vendorOrders.stream().mapToDouble(Order::getPlatformCommissionAmount).sum();

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

    @Override
    public VendorEarningsDTO getMyVendorEarnings(String token, String period) {
        VendorResponseDTO vendor = vendorClient.getCurrentVendor(token);
        List<VendorEarningsDTO> all = getVendorEarningsByPeriod(period);

        return all.stream()
                .filter(e -> e.getVendorId().equals(vendor.getId()))
                .findFirst()
                .orElse(new VendorEarningsDTO(vendor.getId(), vendor.getCompanyName(),
                        vendor.getUpiId(), 0, 0));
    }

    
    @Override
    public List<VendorEarningsDTO> getVendorEarnings(String fromDate, String toDate) {
        LocalDateTime from = LocalDateTime.parse(fromDate + "T00:00:00");
        LocalDateTime to = LocalDateTime.parse(toDate + "T23:59:59");

        List<Order> orders = orderRepository.findByStatusAndOrderDateBetween("DELIVERED", from, to);

        Map<Long, List<Order>> grouped = orders.stream()
                .collect(Collectors.groupingBy(Order::getVendorId));

        return grouped.entrySet().stream().map(entry -> {
            Long vendorId = entry.getKey();
            List<Order> vendorOrders = entry.getValue();

            double totalSales = vendorOrders.stream().mapToDouble(Order::getTotalAmount).sum();
            double commission = vendorOrders.stream().mapToDouble(Order::getPlatformCommissionAmount).sum();

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
    @Override
    public VendorPayoutPreviewDTO previewVendorUnpaid(Long vendorId) {
        LocalDateTime now   = LocalDateTime.now();
        LocalDateTime since = resolveLastPayoutToTime(vendorId).orElse(SQL_MIN);

        List<Order> rows = orderRepository.findDeliveredUnpaidSince(
            vendorId, since, now, VendorSettlementStatus.UNPAID
        );

        VendorResponseDTO v = vendorClient.getVendorById(systemJwtToken, vendorId);

        List<VendorEarningLineDTO> items = rows.stream().map(o -> {
            BigDecimal gross      = BigDecimal.valueOf(o.getTotalAmount() == null ? 0.0 : o.getTotalAmount());
            BigDecimal commission = BigDecimal.valueOf(o.getPlatformCommissionAmount() == null ? 0.0 : o.getPlatformCommissionAmount());
            BigDecimal penalty    = (o.getVendorPenaltyAmount() == null)    ? BigDecimal.ZERO : o.getVendorPenaltyAmount();
            BigDecimal adjustment = (o.getVendorAdjustmentAmount() == null) ? BigDecimal.ZERO : o.getVendorAdjustmentAmount();
            BigDecimal net        = gross.subtract(commission).subtract(penalty).add(adjustment);

            return new VendorEarningLineDTO(
                o.getId(),
                (o.getDeliveredAt() == null ? null
                    : o.getDeliveredAt().atZone(java.time.ZoneId.systemDefault()).toInstant()),
                gross, commission, penalty, adjustment, net, "UNPAID"
            );
        }).collect(java.util.stream.Collectors.toList());

        java.time.Instant sinceInstant = (since.equals(SQL_MIN))
            ? null
            : since.atZone(java.time.ZoneId.systemDefault()).toInstant();

        return new VendorPayoutPreviewDTO(
            sinceInstant,
            v.getCompanyName(),
            v.getUpiId(),
            items
        );
    }

    @Override
    @jakarta.transaction.Transactional
    public VendorPayoutResponseDTO payVendorUnpaid(Long vendorId, String note) {
        LocalDateTime now   = LocalDateTime.now();
        LocalDateTime since = resolveLastPayoutToTime(vendorId).orElse(SQL_MIN);

        List<Order> rows = orderRepository.findDeliveredUnpaidSince(
            vendorId, since, now, VendorSettlementStatus.UNPAID
        );

        if (rows.isEmpty()) {
            return new VendorPayoutResponseDTO(
                null,
                vendorId,
                (since.equals(SQL_MIN) ? null : since.atZone(ZoneId.systemDefault()).toInstant()),
                now.atZone(ZoneId.systemDefault()).toInstant(),
                0,
                BigDecimal.ZERO
            );
        }

        BigDecimal total = rows.stream().map(o -> {
            BigDecimal gross      = BigDecimal.valueOf(o.getTotalAmount() == null ? 0.0 : o.getTotalAmount());
            BigDecimal commission = BigDecimal.valueOf(o.getPlatformCommissionAmount() == null ? 0.0 : o.getPlatformCommissionAmount());
            BigDecimal penalty    = (o.getVendorPenaltyAmount() == null)    ? BigDecimal.ZERO : o.getVendorPenaltyAmount();
            BigDecimal adjustment = (o.getVendorAdjustmentAmount() == null) ? BigDecimal.ZERO : o.getVendorAdjustmentAmount();
            return gross.subtract(commission).subtract(penalty).add(adjustment);
        }).reduce(BigDecimal.ZERO, BigDecimal::add);

        // ✅ never send null: first window starts at epoch
        Instant fromInstant = since.equals(SQL_MIN)
            ? Instant.EPOCH
            : since.atZone(ZoneId.systemDefault()).toInstant();
        Instant toInstant   = now.atZone(ZoneId.systemDefault()).toInstant();

     // compute idempotency window hash (no checked exception leaks)
        String hash;
        try {
            hash = HexFormat.of().formatHex(
                java.security.MessageDigest.getInstance("SHA-256")
                    .digest((vendorId + "|" + fromInstant + "|" + toInstant).getBytes(java.nio.charset.StandardCharsets.UTF_8))
            );
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available on this JRE", e);
        }

        CreateVendorPayoutRequest req = new CreateVendorPayoutRequest();
        req.setVendorId(vendorId);
        req.setFrom(fromInstant);
        req.setTo(toInstant);
        req.setCount(rows.size());
        req.setTotalAmount(total);
        req.setNote(note);
        req.setWindowHash(hash);

        var payout = paymentClient.createVendorPayout(getInternalToken(), req);

        List<Long> ids = rows.stream().map(Order::getId).toList();
        orderRepository.markOrdersPaid(ids, payout.getPayoutId(), VendorSettlementStatus.PAID);

        return payout;
    }
 // put this anywhere inside OrderServiceImpl (e.g., near other helpers)
    private static MessageDigest sha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private java.util.Optional<LocalDateTime> resolveLastPayoutToTime(Long vendorId) {
        LocalDateTime maxDeliveredPaid = orderRepository.findMaxDeliveredAtWherePaid(vendorId, VendorSettlementStatus.PAID);
        return Optional.ofNullable(maxDeliveredPaid);
    }

    @Override
    public List<OrderResponse> getOrdersAvailableForDelivery(String token) {
        return orderRepository.findAll().stream()
            .filter(o -> o.getDeliveryBoyId() == null
                      && o.getOrderItems().stream().allMatch(i -> i.getApprovalStatus() == ApprovalStatus.APPROVED))
            .map(this::toEnrichedResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getAssignedOrdersForRider(String token) {
        DeliveryBoyDTO me = deliveryBoyClient.getCurrentDeliveryBoy(token);
        return orderRepository.findAll().stream()
            .filter(o -> me.getId().equals(o.getDeliveryBoyId()))
            .map(this::toEnrichedResponse)
            .collect(Collectors.toList());
    }
 // OrderServiceImpl.java
    private OrderResponse toEnrichedResponse(Order order) {
        OrderResponse resp = new OrderResponse(order);

        // 1) Customer info
        try {
            UserDTO user = userClient.getUserById(systemJwtToken, order.getUserId());
            String fullName = ((user.getFirstName() == null ? "" : user.getFirstName()) + " " +
                               (user.getLastName()  == null ? "" : user.getLastName())).trim();
            resp.setUserName(fullName.isBlank() ? "Customer" : fullName);
            resp.setUserPhone(user.getMobileNumber());
        } catch (Exception e) {
            System.err.println("Could not load user for order " + order.getId() + ": " + e.getMessage());
        }

        // 2) Address lat/lng for user pin + ETA
        try {
            if (order.getAddressId() != null) {
                AddressDTO addr = addressClient.getAddressById(systemJwtToken, order.getAddressId());
                if (addr != null) {
                    resp.setAddressLat(addr.getLatitude());
                    resp.setAddressLng(addr.getLongitude());
                    // Keep shippingAddress already set on order
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load address for order " + order.getId() + ": " + e.getMessage());
        }

        // 3) Vendor location and phone
        try {
            if (order.getVendorId() != null) {
                VendorResponseDTO v = vendorClient.getVendorById(systemJwtToken, order.getVendorId());
                resp.setVendorLat(v.getLatitude());
                resp.setVendorLng(v.getLongitude());

                // 🔑 Populate vendor phone at both order and item level
                String phone = v.getContactNumber(); // adjust if field name differs
                resp.setVendorPhone(phone);

                if (resp.getItems() != null) {
                    for (OrderItemDTO it : resp.getItems()) {
                        it.setVendorPhone(phone);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load vendor for order " + order.getId() + ": " + e.getMessage());
        }

        // 4) Compact product summary shown on cards (first name + total qty)
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            String firstName = order.getOrderItems().get(0).getProductName();
            int totalQty = order.getOrderItems().stream()
                    .mapToInt(oi -> oi.getQuantity() == null ? 0 : oi.getQuantity())
                    .sum();
            try {
                var firstNameField = OrderResponse.class.getDeclaredField("firstProductName");
                firstNameField.setAccessible(true);
                firstNameField.set(resp, firstName);
            } catch (Exception ignore) {}
            try {
                var qtyField = OrderResponse.class.getDeclaredField("totalQuantity");
                qtyField.setAccessible(true);
                qtyField.set(resp, totalQty);
            } catch (Exception ignore) {}
        }
     // OrderServiceImpl.java  -> inside toEnrichedResponse(Order order) just before 'return resp;'
        try {
            if (order.getDeliveryBoyId() != null) {
                DeliveryBoyDTO rider = deliveryBoyClient.getById(systemJwtToken, order.getDeliveryBoyId());
                if (rider != null) {
                    String fullRiderName = ((rider.getFirstName() == null ? "" : rider.getFirstName()) + " " +
                                            (rider.getLastName() == null ? "" : rider.getLastName())).trim();
                    resp.setDeliveryBoyName(fullRiderName.isBlank() ? "Delivery Partner" : fullRiderName);
                    resp.setDeliveryBoyPhone(rider.getPhone());
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load delivery boy for order " + order.getId() + ": " + e.getMessage());
        }

        return resp;
    }



    // Runs every 30s; approve anything older than delaySeconds
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void autoApprovePendingItemsJob() {
        if (!autoApproveEnabled) return;

        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(autoApproveDelaySeconds);
        List<OrderItem> pending = orderItemRepository.findPendingOlderThan(ApprovalStatus.PENDING, cutoff);
        if (pending.isEmpty()) return;

        // flip items to APPROVED
        for (OrderItem it : pending) {
            it.setApprovalStatus(ApprovalStatus.APPROVED);
            // keep item-level delivery status unchanged here; your normal flow sets it later
            // touch the order.updatedAt to reflect change
            if (it.getOrder() != null) {
                it.getOrder().setUpdatedAt(LocalDateTime.now());
            }
        }

        orderItemRepository.saveAll(pending);

        // (Optional) If you want to notify vendors/admin here, do it in a try/catch.
        System.out.println("✅ Auto-approved " + pending.size() + " order items older than " + autoApproveDelaySeconds + "s");
    }

    
 // ====== EMAIL HTML TEMPLATES ======
    private static final NumberFormat INR = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    private String buildOrderHtml(Order order, String heading, String subtitle, List<OrderItem> items, String actionUrl, String actionText) {
        StringBuilder rows = new StringBuilder();
        for (OrderItem it : items) {
            rows.append("""
                <tr style="border-bottom:1px solid #eee">
                  <td style="padding:8px 0">
                    <img src="%s" width="64" height="64" style="object-fit:cover;border-radius:8px" onerror="this.style.display='none'"/>
                  </td>
                  <td style="padding:8px 0; font-size:14px">
                    <div style="font-weight:600">%s</div>
                    <div style="color:#666">Qty: %d</div>
                  </td>
                  <td style="padding:8px 0; text-align:right; white-space:nowrap">%s</td>
                  <td style="padding:8px 0; text-align:right; white-space:nowrap">%s</td>
                </tr>
            """.formatted(
                safe(it.getProductImageUrl()),
                safe(it.getProductName()),
                (it.getQuantity()==null?0:it.getQuantity()),
                INR.format(it.getPrice()==null?0:it.getPrice()),
                INR.format(it.getTotalPrice()==null?0:it.getTotalPrice())
            ));
        }

        String address = safe(order.getShippingAddress());
        String summary = """
            <tr><td colspan="3" style="padding:4px 0; color:#666">Items total</td><td style="text-align:right">%s</td></tr>
            <tr><td colspan="3" style="padding:4px 0; color:#666">Delivery fee</td><td style="text-align:right">%s</td></tr>
            <tr><td colspan="3" style="padding:8px 0; font-weight:700">Grand total</td><td style="text-align:right; font-weight:700">%s</td></tr>
        """.formatted(
            INR.format(order.getTotalAmount()),
            INR.format(order.getDeliveryFee()==null?0:order.getDeliveryFee()),
            INR.format(order.getFinalAmount()==null?order.getTotalAmount():order.getFinalAmount())
        );

        return """
        <div style="font-family:Inter,Segoe UI,Arial,sans-serif; background:#f6f7f9; padding:24px">
          <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:640px; margin:0 auto; background:#fff; border-radius:14px; box-shadow:0 2px 10px rgba(0,0,0,.06); overflow:hidden">
            <tr><td style="padding:20px 24px; background:#1e7a46; color:#fff">
              <div style="font-size:18px; font-weight:700">%s</div>
              <div style="opacity:.9; font-size:14px">%s</div>
            </td></tr>
            <tr><td style="padding:18px 24px">
              <div style="font-size:14px; color:#333; margin-bottom:8px">Order ID: <b>#%d</b></div>
              <div style="font-size:13px; color:#666; margin-bottom:18px">Placed on: %s</div>
              <table width="100%%" cellspacing="0" cellpadding="0" style="border-collapse:collapse">%s</table>
              <table width="100%%" cellspacing="0" cellpadding="0" style="margin-top:10px; border-top:1px dashed #e5e7eb; padding-top:10px">%s</table>
              <div style="margin-top:18px; font-size:13px; color:#444">
                <div style="font-weight:600; margin-bottom:4px">Delivery address</div>
                <div style="white-space:pre-line">%s</div>
              </div>
              <div style="margin:24px 0">
                <a href="%s" style="display:inline-block; background:#1e7a46; color:#fff; text-decoration:none; padding:10px 16px; border-radius:10px; font-weight:600">%s</a>
              </div>
              <div style="font-size:12px; color:#9aa3af">This is an automated email from CaroCart.</div>
            </td></tr>
          </table>
        </div>
        """.formatted(
            safe(heading),
            safe(subtitle),
            order.getId(),
            safe(order.getOrderDate()==null?LocalDateTime.now().toString():order.getOrderDate().toString()),
            rows.toString(),
            summary,
            address,
            safe(actionUrl),
            safe(actionText)
        );
    }

    private static String safe(String s){ return s==null? "": s; }

}
