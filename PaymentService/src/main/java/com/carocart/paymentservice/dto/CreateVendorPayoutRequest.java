package com.carocart.paymentservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class CreateVendorPayoutRequest {
  private Long vendorId;
  private Instant from;            // inclusive
  private Instant to;              // inclusive / now
  private Integer count;           // number of entries
  private BigDecimal totalAmount;  // sum of net amounts
  private String note;
  private String windowHash;       // optional idempotency
  private List<PayoutItemDTO> items; // optional snapshot list

  public static class PayoutItemDTO {
    private Long orderId;
    private BigDecimal netAmount;
    private Instant deliveredAt;
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }
  }

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
  public List<PayoutItemDTO> getItems() { return items; }
  public void setItems(List<PayoutItemDTO> items) { this.items = items; }
}
