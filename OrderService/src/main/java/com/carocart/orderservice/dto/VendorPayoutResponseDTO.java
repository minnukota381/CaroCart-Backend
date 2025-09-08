package com.carocart.orderservice.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class VendorPayoutResponseDTO implements Serializable {
  private String payoutId;
  private Long vendorId;
  private Instant from;
  private Instant to;
  private Integer count;
  private BigDecimal totalAmount;

  public VendorPayoutResponseDTO() {}
  public VendorPayoutResponseDTO(String payoutId, Long vendorId, Instant from, Instant to,
                                 Integer count, BigDecimal totalAmount) {
    this.payoutId = payoutId;
    this.vendorId = vendorId;
    this.from = from;
    this.to = to;
    this.count = count;
    this.totalAmount = totalAmount;
  }
  // getters/setters…
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
}
