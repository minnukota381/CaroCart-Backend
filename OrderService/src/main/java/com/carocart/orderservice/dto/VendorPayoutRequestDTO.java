package com.carocart.orderservice.dto;

import java.io.Serializable;

public class VendorPayoutRequestDTO implements Serializable {
  private String note; // admin note for audit

  public VendorPayoutRequestDTO() {}
  public VendorPayoutRequestDTO(String note) { this.note = note; }
  public String getNote() { return note; }
  public void setNote(String note) { this.note = note; }
  
}

