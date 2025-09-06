package com.carocart.paymentservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class VendorPayoutDTO {
  private String payoutId;
  private Long vendorId;
  private Instant from;
  private Instant to;
  private Integer count;
  private BigDecimal totalAmount;
  private String status;

  public String getPayoutId() { return payoutId; }
  public void setPayoutId(String payoutId) { this.payoutId = payoutId; }
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
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
