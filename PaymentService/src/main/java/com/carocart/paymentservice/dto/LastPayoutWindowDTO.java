package com.carocart.paymentservice.dto;

import java.time.Instant;

public class LastPayoutWindowDTO {
  private Instant lastTo;
  public LastPayoutWindowDTO() {}
  public LastPayoutWindowDTO(Instant lastTo) { this.lastTo = lastTo; }
  public Instant getLastTo() { return lastTo; }
  public void setLastTo(Instant lastTo) { this.lastTo = lastTo; }
}
