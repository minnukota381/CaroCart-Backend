package com.carocart.paymentservice.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class VendorPayoutHistoryItemDTO implements Serializable {
  private String payoutId;
  private Instant from;
  private Instant to;
  private Integer count;
  private BigDecimal totalAmount;
  private String status;

  public VendorPayoutHistoryItemDTO() {}
  public VendorPayoutHistoryItemDTO(String payoutId, Instant from, Instant to,
                                    Integer count, BigDecimal totalAmount, String status) {
    this.payoutId = payoutId; this.from = from; this.to = to;
    this.count = count; this.totalAmount = totalAmount; this.status = status;
  }
  // getters/setters…
  public String getPayoutId() { return payoutId; }
  public void setPayoutId(String payoutId) { this.payoutId = payoutId; }
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
