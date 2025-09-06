package com.carocart.orderservice.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public class VendorPayoutPreviewDTO implements Serializable {
  private Instant since;      // last payout end; null if first payout
  private String vendorName;  // for header
  private String upiId;       // for header
  private List<VendorEarningLineDTO> items;

  public VendorPayoutPreviewDTO() {}
  public VendorPayoutPreviewDTO(Instant since, String vendorName, String upiId, List<VendorEarningLineDTO> items) {
    this.since = since;
    this.vendorName = vendorName;
    this.upiId = upiId;
    this.items = items;
  }
  // getters/setters…
  public Instant getSince() { return since; }
  public void setSince(Instant since) { this.since = since; }
  public String getVendorName() { return vendorName; }
  public void setVendorName(String vendorName) { this.vendorName = vendorName; }
  public String getUpiId() { return upiId; }
  public void setUpiId(String upiId) { this.upiId = upiId; }
  public List<VendorEarningLineDTO> getItems() { return items; }
  public void setItems(List<VendorEarningLineDTO> items) { this.items = items; }
}
