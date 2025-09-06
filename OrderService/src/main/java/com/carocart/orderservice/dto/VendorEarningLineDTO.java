package com.carocart.orderservice.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class VendorEarningLineDTO implements Serializable {
  private Long orderId;
  private Instant deliveredAt;
  private BigDecimal grossAmount;       // vendor-side gross
  private BigDecimal commissionAmount;  // platform commission
  private BigDecimal penaltyAmount;     // optional (0 if none)
  private BigDecimal adjustmentAmount;  // optional (manual +/-)
  private BigDecimal netAmount;         // gross - commission - penalty + adjustment
  private String status;                // "UNPAID" | "PAID"

  public VendorEarningLineDTO() {}

  public VendorEarningLineDTO(
      Long orderId,
      Instant deliveredAt,
      BigDecimal grossAmount,
      BigDecimal commissionAmount,
      BigDecimal penaltyAmount,
      BigDecimal adjustmentAmount,
      BigDecimal netAmount,
      String status
  ) {
    this.orderId = orderId;
    this.deliveredAt = deliveredAt;
    this.grossAmount = grossAmount;
    this.commissionAmount = commissionAmount;
    this.penaltyAmount = penaltyAmount;
    this.adjustmentAmount = adjustmentAmount;
    this.netAmount = netAmount;
    this.status = status;
  }

  public Long getOrderId() { return orderId; }
  public void setOrderId(Long orderId) { this.orderId = orderId; }

  public Instant getDeliveredAt() { return deliveredAt; }
  public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }

  public BigDecimal getGrossAmount() { return grossAmount; }
  public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }

  public BigDecimal getCommissionAmount() { return commissionAmount; }
  public void setCommissionAmount(BigDecimal commissionAmount) { this.commissionAmount = commissionAmount; }

  public BigDecimal getPenaltyAmount() { return penaltyAmount; }
  public void setPenaltyAmount(BigDecimal penaltyAmount) { this.penaltyAmount = penaltyAmount; }

  public BigDecimal getAdjustmentAmount() { return adjustmentAmount; }
  public void setAdjustmentAmount(BigDecimal adjustmentAmount) { this.adjustmentAmount = adjustmentAmount; }

  public BigDecimal getNetAmount() { return netAmount; }
  public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
