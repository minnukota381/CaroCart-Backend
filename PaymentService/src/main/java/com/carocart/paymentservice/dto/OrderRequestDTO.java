package com.carocart.paymentservice.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OrderRequestDTO {
	 @JsonProperty("userId")
	    private Long userId;
	    @JsonProperty("addressId")
	    private Long addressId;
	    @JsonProperty("shippingAddress")
	    private String shippingAddress;
	    @JsonProperty("items")
	    private List<OrderItemDTO> items;
	    @JsonProperty("totalAmount")
	    private Double totalAmount;

    public OrderRequestDTO() {}

    // --- Getters and Setters ---

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
}
