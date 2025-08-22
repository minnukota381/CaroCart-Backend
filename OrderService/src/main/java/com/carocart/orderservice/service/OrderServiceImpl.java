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
 // fields
    private final DeliveryEarningClient deliveryEarningClient;

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
    	    JwtUtil jwtUtil,
    	    DeliveryEarningClient deliveryEarningClient
    	    

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
        if (km <= 3)  return 19.0;
        if (km <= 6)  return 29.0;
        if (km <= 9)  return 39.0;
        if (km <= 12) return 59.0;
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
    public OrderResponse placeOrder(String token, OrderRequest orderRequest) {
        // 1) user + cart
        UserDTO user = userClient.getCurrentUser(token);
        List<CartItemDTO> cartItems = cartClient.getUserCart(token);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty. Cannot place order.");
        }

        // 2) address
        AddressDTO address = addressClient.getAddressById(orderRequest.getAddressId(), token);

        // 3) order shell
        Order order = new Order();
        order.setUserId(user.getId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PLACED");
        order.setAddressId(orderRequest.getAddressId());
        order.setShippingAddress(address.getAddress());

        // payment
        if ("COD".equalsIgnoreCase(orderRequest.getPaymentMethod())) {
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

        VendorResponseDTO vendor = vendorClient.getVendorById(systemJwtToken, firstProduct.getVendorId());

        // 5) items
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
        double totalAmount = orderItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();
        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setPlatformCommissionPct(20.0);
        order.setPlatformCommissionAmount(totalAmount * 0.20);

        System.out.println("Vendor location: " + vendor.getLatitude() + "," + vendor.getLongitude());
        System.out.println("User location: " + address.getLatitude() + "," + address.getLongitude());

        // ✅ New delivery-fee logic (customer-facing)
        double distanceKm = distanceKmForOrder(order.getVendorId(), order.getAddressId());
        boolean isGrocery = isGroceryVendor(order.getVendorId());

        double customerFee = feeFromSlab(distanceKm);                  // slab price
        if (qualifiesForFree(totalAmount, isGrocery)) customerFee = 0; // free above threshold

        order.setDeliveryFee(customerFee);                              // what customer pays
        order.setFinalAmount(totalAmount + customerFee);

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
    public OrderResponse placeOrderInternal(OrderRequest orderRequest) {
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

        Long firstProductId = cartItems.get(0).getProductId();
        ProductDTO firstProduct = productClient.getProductById(firstProductId);
        if (firstProduct.getVendorId() == null) {
            throw new RuntimeException("Vendor ID is null for product: " + firstProductId);
        }
        order.setVendorId(firstProduct.getVendorId());

        VendorResponseDTO vendor = vendorClient.getVendorById(systemJwtToken, firstProduct.getVendorId());
        AddressDTO address = addressClient.getAddressById(orderRequest.getAddressId(), systemJwtToken);

        // items
        List<OrderItem> orderItems = cartItems.stream().map(item -> {
            ProductDTO product = productClient.getProductById(item.getProductId());
            productClient.decreaseStock(product.getId(), item.getQuantity());

            OrderItem oi = new OrderItem();
            oi.setProductId(product.getId());
            oi.setProductName(product.getName());
            oi.setProductImageUrl(product.getImageUrl());
            oi.setQuantity(item.getQuantity());
            oi.setPrice(item.getPrice());
            oi.setTotalPrice(item.getPrice() * item.getQuantity());
            oi.setOrder(order);
            oi.setApprovalRequiredBy(product.getVendorEmail() != null ? "VENDOR" : "ADMIN");
            oi.setVendorEmail(product.getVendorEmail());
            oi.setApprovalStatus(ApprovalStatus.PENDING);
            return oi;
        }).collect(Collectors.toList());

        // totals (trust the request total but also compute to keep finalAmount consistent)
        double itemsTotal = orderItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();
        order.setOrderItems(orderItems);
        order.setTotalAmount(itemsTotal);
        order.setPlatformCommissionPct(20.0);
        order.setPlatformCommissionAmount(itemsTotal * 0.20);

        System.out.println("Vendor location: " + vendor.getLatitude() + "," + vendor.getLongitude());
        System.out.println("User location: " + address.getLatitude() + "," + address.getLongitude());

        // ✅ New delivery-fee logic (customer-facing)
        double distanceKm = distanceKmForOrder(order.getVendorId(), order.getAddressId());
        boolean isGrocery = isGroceryVendor(order.getVendorId());

        double customerFee = feeFromSlab(distanceKm);
        if (qualifiesForFree(itemsTotal, isGrocery)) customerFee = 0;

        order.setDeliveryFee(customerFee);
        order.setFinalAmount(itemsTotal + customerFee);

        return new OrderResponse(orderRepository.save(order));
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
         // ✅ Vendor location + phone
            try {
                ProductDTO product = productClient.getProductById(item.getProductId());
                if (product != null && product.getVendorId() != null) {
                    VendorResponseDTO vendor = vendorClient.getVendorById(systemJwtToken, product.getVendorId());
                    if (vendor != null) {
                        dto.setVendorLat(vendor.getLatitude());
                        dto.setVendorLng(vendor.getLongitude());
                        dto.setVendorPhone(vendor.getContactNumber());   // ✅ add this line
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
    @Transactional
    public void updateOrderDeliveryStatus(String token, Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Update delivery_status instead of status
        order.setDeliveryStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Update delivery_status in all items of this order
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            item.setDeliveryStatus(status);
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
        if (allDelivered) {
            order.setStatus("DELIVERED");
        } else if (allOutForDelivery) {
            order.setStatus("OUT_FOR_DELIVERY");
        }
        if (order.getDeliveryBoyId() == null && item.getDeliveryBoyId() != null) {
            order.setDeliveryBoyId(item.getDeliveryBoyId());
        }
        order.setDeliveryStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // ✅ Create earning when an item reaches DELIVERED
     // ... after you saved item & order and computed allDelivered
     // After you saved item & order and computed allDelivered
        if ("DELIVERED".equalsIgnoreCase(status) && allDelivered) {
            try {
                // Reference one stable item id (min id) so repeats don't create duplicates
                long referenceItemId = items.stream().mapToLong(OrderItem::getId).min().orElse(item.getId());

                // If an earning for this order is already made, your Delivery service will no-op
                // because it de-duplicates on orderItemId (we'll always use the same reference).

                // Full rider payout = slab by distance (independent of customer fee)
                double distanceKm = distanceKmForOrder(order.getVendorId(), order.getAddressId());
                double payout = riderPayoutByDistance(distanceKm);

                Long boyId = item.getDeliveryBoyId() != null ? item.getDeliveryBoyId() : order.getDeliveryBoyId();
                if (boyId == null) {
                    System.err.println("⚠️ Skipping earning creation: no deliveryBoyId set.");
                } else {
                    var req = new com.carocart.orderservice.dto.DeliveryEarningCreateRequest(
                            referenceItemId,   // stable id → idempotent at Delivery service
                            boyId,
                            payout,            // FULL payout (₹19/29/39/59 by distance)
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
}
