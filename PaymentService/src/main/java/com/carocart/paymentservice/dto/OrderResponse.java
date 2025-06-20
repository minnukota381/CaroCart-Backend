package com.carocart.paymentservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    private Long orderId;
    private Long userId;
    private LocalDateTime orderDate;
    private String status;
    private Double totalAmount;
    private String shippingAddress;
    private List<OrderItemResponseDTO> orderItems;


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


	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}


	public Double getTotalAmount() {
        return totalAmount;
    }


}
