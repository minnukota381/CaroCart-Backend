// Updated: OrderServiceImpl.java with full address retrieval

package com.carocart.orderservice.service;

import com.carocart.orderservice.dto.*;
import com.carocart.orderservice.entity.Order;
import com.carocart.orderservice.entity.OrderItem;
import com.carocart.orderservice.feign.AddressClient;
import com.carocart.orderservice.feign.CartClient;
import com.carocart.orderservice.feign.ProductClient;
import com.carocart.orderservice.feign.UserClient;
import com.carocart.orderservice.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            CartClient cartClient,
                            ProductClient productClient,
                            UserClient userClient,
                            AddressClient addressClient) {
        this.orderRepository = orderRepository;
        this.cartClient = cartClient;
        this.productClient = productClient;
        this.userClient = userClient;
        this.addressClient = addressClient;
    }


    @Override
    @Transactional
    public OrderResponse placeOrder(String token, OrderRequest orderRequest) {
        UserDTO user = userClient.getCurrentUser(token);
        List<CartItemDTO> cartItems = cartClient.getUserCart(token);

        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty. Cannot place order.");
        }

        // Fetch and format the shipping address
        AddressDTO address = addressClient.getAddressById(orderRequest.getAddressId(), token);
        String formattedAddress = address.toFormattedString(); // assume this method exists

        // Create the order entity
        Order order = new Order();
        order.setUserId(user.getId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PLACED");
        order.setShippingAddress(formattedAddress);
        order.setPaymentStatus("PENDING");

        // Prepare order items
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

        double totalAmount = orderItems.stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        cartClient.clearCart(token);

        return new OrderResponse(savedOrder);
    }

    private String formatAddress(AddressDTO address) {
        return String.format("%s, %s, %s, %s, %s - %s (%s)",
                address.getHouseNumber(),
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getLandmark(),
                address.getPincode(),
                address.getAddressType());
    }

    @Override
    public List<OrderResponse> getOrdersForUser(String token) {
        UserDTO user = userClient.getCurrentUser(token);
        List<Order> orders = orderRepository.findByUserId(user.getId());
        return orders.stream().map(OrderResponse::new).collect(Collectors.toList());
    }

    @Override
    public OrderResponse getOrderById(String token, Long orderId) {
        UserDTO user = userClient.getCurrentUser(token);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found."));

        if (!order.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to this order.");
        }

        return new OrderResponse(order);
    }

    @Override
    public void cancelOrder(String token, Long orderId) {
        UserDTO user = userClient.getCurrentUser(token);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found."));

        if (!order.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to cancel this order.");
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(OrderResponse::new).collect(Collectors.toList());
    }

    @Override
    public OrderResponse getOrderByIdForAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found."));
        return new OrderResponse(order);
    }

    @Override
    public void cancelOrderByAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found."));

        if ("CANCELLED".equalsIgnoreCase(order.getStatus()) ||
                "CANCELLED BY ADMIN".equalsIgnoreCase(order.getStatus())) {
            throw new RuntimeException("Order is already cancelled.");
        }

        order.setStatus("CANCELLED BY ADMIN");
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void updatePaymentStatus(Long orderId, String paymentId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order with ID " + orderId + " not found."));

        order.setPaymentId(paymentId);
        order.setPaymentStatus(status.toUpperCase());
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void markAsCod(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order with ID " + orderId + " not found."));

        order.setPaymentId("COD-" + orderId);
        order.setPaymentStatus("COD");
        order.setStatus("PLACED");
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);
    }
}
