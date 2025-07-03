package com.carocart.orderservice.service;

import com.carocart.orderservice.dto.*;
import com.carocart.orderservice.entity.Order;
import com.carocart.orderservice.entity.OrderItem;
import com.carocart.orderservice.feign.*;
import com.carocart.orderservice.repository.OrderRepository;
import com.carocart.orderservice.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final ProductClient productClient;
    private final UserClient userClient;
    private final AddressClient addressClient;
    private final NotificationClient notificationClient;
    private final JwtUtil jwtUtil;

    private String internalToken;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CartClient cartClient,
                            ProductClient productClient,
                            UserClient userClient,
                            AddressClient addressClient,
                            NotificationClient notificationClient,
                            JwtUtil jwtUtil) {
        this.orderRepository = orderRepository;
        this.cartClient = cartClient;
        this.productClient = productClient;
        this.userClient = userClient;
        this.addressClient = addressClient;
        this.notificationClient = notificationClient;
        this.jwtUtil = jwtUtil;
    }

    @PostConstruct
    public void init() {
        // 👇 Generate internal ADMIN JWT token
        this.internalToken = "Bearer " + jwtUtil.generateToken("system@carocart.com", "ADMIN", "System", "Service");
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(String token, OrderRequest orderRequest) {
        UserDTO user = userClient.getCurrentUser(token);
        List<CartItemDTO> cartItems = cartClient.getUserCart(token);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty. Cannot place order.");
        }

        AddressDTO address = addressClient.getAddressById(orderRequest.getAddressId(), token);
        String formattedAddress = address.toFormattedString();

        Order order = new Order();
        order.setUserId(user.getId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PLACED");
        order.setShippingAddress(formattedAddress);
        order.setPaymentStatus("PENDING");

        List<OrderItem> orderItems = cartItems.stream().map(item -> {
            ProductDTO product = productClient.getProductById(item.getProductId());
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setTotalPrice(product.getPrice() * item.getQuantity());
            orderItem.setOrder(order);
            return orderItem;
        }).collect(Collectors.toList());

        double totalAmount = orderItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();
        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        // For delivery assignment
        order.setDeliveryStatus("READY_FOR_DELIVERY");
        order.setDeliveryBoyId(null);

        Order savedOrder = orderRepository.save(order);
        cartClient.clearCart(token);

        sendOrderPlacedEmails(savedOrder, user, orderItems);

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
        UserDTO user = userClient.getCurrentUserById(internalToken, order.getUserId());

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
        return orderRepository.findByUserId(user.getId())
                .stream().map(OrderResponse::new).collect(Collectors.toList());
    }

    @Override
    public OrderResponse getOrderById(String token, Long orderId) {
        UserDTO user = userClient.getCurrentUser(token);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!order.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access.");
        }
        return new OrderResponse(order);
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
        // Delivery status reset
        order.setDeliveryStatus("READY_FOR_DELIVERY");
        order.setDeliveryBoyId(null);

        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void markOrderAsShipped(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus("SHIPPED");
        // Optionally, you can trigger delivery status to READY_FOR_DELIVERY here too
        order.setDeliveryStatus("READY_FOR_DELIVERY");
        order.setDeliveryBoyId(null);

        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        sendOrderShippedEmails(order);
    }
    
    @Override
    public long getTotalOrdersCount() {
        return orderRepository.count();
    }

    @Override
    @Transactional
    public OrderResponse placeOrderInternal(OrderRequest orderRequest) {
        List<OrderItemDTO> cartItems = orderRequest.getItems();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty. Cannot place order.");
        }

        String formattedAddress = orderRequest.getShippingAddress() != null
            ? orderRequest.getShippingAddress()
            : "Unknown Address";

        Order order = new Order();
        order.setUserId(orderRequest.getUserId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PLACED");
        order.setShippingAddress(formattedAddress);
        order.setPaymentStatus("PAID");
        order.setTotalAmount(orderRequest.getTotalAmount());

        // For delivery assignment
        order.setDeliveryStatus("READY_FOR_DELIVERY");
        order.setDeliveryBoyId(null);

        // --- Create OrderItems ---
        List<OrderItem> orderItems = cartItems.stream().map(item -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(item.getProductId());
            orderItem.setProductName(item.getProductName() != null ? item.getProductName() : "Product");
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getPrice());
            orderItem.setTotalPrice(item.getPrice() * item.getQuantity());
            orderItem.setOrder(order);
            return orderItem;
        }).collect(Collectors.toList());

        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        return new OrderResponse(savedOrder);
    }

    // ===== DELIVERY BOY SELF-ASSIGNMENT FLOW (NEW) =====

    @Override
    public List<OrderResponse> getReadyForDeliveryOrders() {
        // Orders ready for delivery and not assigned
        return orderRepository.findAll().stream()
                .filter(o -> "READY_FOR_DELIVERY".equalsIgnoreCase(o.getDeliveryStatus()) && o.getDeliveryBoyId() == null)
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignOrderToDeliveryBoy(Long orderId, Long deliveryBoyId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getDeliveryBoyId() != null) {
            throw new RuntimeException("Order already assigned to a delivery boy.");
        }
        order.setDeliveryBoyId(deliveryBoyId);
        order.setDeliveryStatus("CLAIMED");
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public List<OrderResponse> getAssignedOrders(Long deliveryBoyId) {
        return orderRepository.findAll().stream()
                .filter(o -> deliveryBoyId.equals(o.getDeliveryBoyId()))
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateDeliveryStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setDeliveryStatus(status.toUpperCase());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }
}
