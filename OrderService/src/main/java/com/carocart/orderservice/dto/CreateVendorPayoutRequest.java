// orderservice/src/main/java/com/carocart/orderservice/dto/CreateVendorPayoutRequest.java
package com.carocart.orderservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class CreateVendorPayoutRequest {
  private Long vendorId;
  private Instant from;
  private Instant to;
  private Integer count;
  private BigDecimal totalAmount;
  private String note;
  private String windowHash; // optional

  // getters/setters
  public Long getVendorId() { return vendorId; }
  public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
  public Instant getFrom() { return from; }
  public void setFrom(Instant from) { this.from = from; }
  public Instant getTo() { return to; }
  public void setTo(Instant to) { this.to = to; }
  public Integer getCount() { return count; }
  public void setCount(Integer count) { this.count = count; }
  public BigDecimal getTotalAmount() { return totalAmount; }
  public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
  public String getNote() { return note; }
  public void setNote(String note) { this.note = note; }
  public String getWindowHash() { return windowHash; }
  public void setWindowHash(String windowHash) { this.windowHash = windowHash; }
}
