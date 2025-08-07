package com.carocart.orderservice.dto;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderRequest {
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
	    private Long vendorId; 

 public OrderRequest() {}

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

public Long getVendorId() {
	return vendorId;
}

public void setVendorId(Long vendorId) {
	this.vendorId = vendorId;
}
 
}
