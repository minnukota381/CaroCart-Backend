package com.carocart.paymentservice.dto;


//For both services, set the correct package.

public class OrderItemDTO {

 private Long productId;
 private String productName;
 private Integer quantity;
 private Double price;
 private Double totalPrice;
 private String approvalRequiredBy;
 private String approvalStatus;
 private String vendorEmail;

 public OrderItemDTO() {}


 // --- Getters and Setters ---
 public Long getProductId() { return productId; }
 public void setProductId(Long productId) { this.productId = productId; }

 public String getProductName() { return productName; }
 public void setProductName(String productName) { this.productName = productName; }

 public Integer getQuantity() { return quantity; }
 public void setQuantity(Integer quantity) { this.quantity = quantity; }

 public Double getPrice() { return price; }
 public void setPrice(Double price) { this.price = price; }

 public Double getTotalPrice() { return totalPrice; }
 public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

 public String getApprovalRequiredBy() { return approvalRequiredBy; }
 public void setApprovalRequiredBy(String approvalRequiredBy) { this.approvalRequiredBy = approvalRequiredBy; }

 public String getApprovalStatus() { return approvalStatus; }
 public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

 public String getVendorEmail() { return vendorEmail; }
 public void setVendorEmail(String vendorEmail) { this.vendorEmail = vendorEmail; }
}
