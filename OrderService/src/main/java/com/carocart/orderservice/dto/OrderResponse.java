package com.carocart.orderservice.dto;

import com.carocart.orderservice.entity.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderResponse {
    private Long orderId;
    private Long userId;
    private LocalDateTime orderDate;
    private String status;
    private Double subtotal;
    private Double shipping;
    private Double tax;
    private Double totalAmount;
    private String shippingAddress;
    private List<OrderItemResponseDTO> orderItems;

    public OrderResponse() {}

    public OrderResponse(Order order) {
        this.orderId = order.getId();
        this.userId = order.getUserId();
        this.orderDate = order.getOrderDate();
        this.status = order.getStatus();
        this.shippingAddress = order.getShippingAddress();

        this.orderItems = order.getOrderItems().stream()
                .map(OrderItemResponseDTO::new)
                .collect(Collectors.toList());

        this.subtotal = calculateSubtotal();
        this.shipping = calculateShipping();
        this.tax = calculateTax();
        this.totalAmount = subtotal + shipping + tax;
    }

    private double calculateSubtotal() {
        return orderItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    private double calculateShipping() {
//        return 30.0; /
    	return 0; // Example flat rate; can be dynamic later
    }

    private double calculateTax() {
//        return subtotal * 0.05; // Example 5% tax
    	return 0;
    }

    // --- Getters and Setters ---

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getShipping() {
        return shipping;
    }

    public void setShipping(Double shipping) {
        this.shipping = shipping;
    }

    public Double getTax() {
        return tax;
    }

    public void setTax(Double tax) {
        this.tax = tax;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public List<OrderItemResponseDTO> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemResponseDTO> orderItems) {
        this.orderItems = orderItems;
    }
}