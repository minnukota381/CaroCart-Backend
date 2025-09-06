package com.carocart.authentication.dto;

import java.io.Serializable;

public class AdminVendorListItemDTO implements Serializable {
  private Long vendorId;
  private String vendorName; // companyName
  private String upiId;

  public AdminVendorListItemDTO() {}
  public AdminVendorListItemDTO(Long vendorId, String vendorName, String upiId) {
    this.vendorId = vendorId;
    this.vendorName = vendorName;
    this.upiId = upiId;
  }
  public Long getVendorId() { return vendorId; }
  public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
  public String getVendorName() { return vendorName; }
  public void setVendorName(String vendorName) { this.vendorName = vendorName; }
  public String getUpiId() { return upiId; }
  public void setUpiId(String upiId) { this.upiId = upiId; }
}

