package com.carocart.paymentservice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "vendor_payouts",
       uniqueConstraints = @UniqueConstraint(name="uq_vendor_window", columnNames = {"vendor_id","from_at","to_at"}),
       indexes = {
         @Index(name="idx_vendor_payout_vendor", columnList = "vendor_id"),
         @Index(name="idx_vendor_payout_created", columnList = "created_at")
       })
public class VendorPayout {

  @Id
  @Column(name = "payout_id", length = 64)
  private String payoutId; // e.g., VP_20250902_000123

  @Column(name = "vendor_id", nullable = false)
  private Long vendorId;

  @Column(name = "from_at", nullable = false)
  private Instant fromAt;

  @Column(name = "to_at", nullable = false)
  private Instant toAt;

  @Column(name = "count", nullable = false)
  private Integer count;

  @Column(name = "total_amount", precision=12, scale=2, nullable = false)
  private BigDecimal totalAmount;

  @Column(name = "note")
  private String note;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 16, nullable = false)
  private PayoutStatus status = PayoutStatus.CREATED; // CREATED|PAID|FAILED

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "window_hash", length = 128)
  private String windowHash;

  // getters & setters
  public String getPayoutId() { return payoutId; }
  public void setPayoutId(String payoutId) { this.payoutId = payoutId; }
  public Long getVendorId() { return vendorId; }
  public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
  public Instant getFromAt() { return fromAt; }
  public void setFromAt(Instant fromAt) { this.fromAt = fromAt; }
  public Instant getToAt() { return toAt; }
  public void setToAt(Instant toAt) { this.toAt = toAt; }
  public Integer getCount() { return count; }
  public void setCount(Integer count) { this.count = count; }
  public BigDecimal getTotalAmount() { return totalAmount; }
  public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
  public String getNote() { return note; }
  public void setNote(String note) { this.note = note; }
  public PayoutStatus getStatus() { return status; }
  public void setStatus(PayoutStatus status) { this.status = status; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public String getWindowHash() { return windowHash; }
  public void setWindowHash(String windowHash) { this.windowHash = windowHash; }

  public enum PayoutStatus { CREATED, PAID, FAILED }
}
