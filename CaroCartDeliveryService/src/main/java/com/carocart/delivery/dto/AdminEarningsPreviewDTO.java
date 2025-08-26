package com.carocart.delivery.dto;

import com.carocart.delivery.entity.DeliveryEarning;
import java.time.LocalDateTime;
import java.util.List;

public class AdminEarningsPreviewDTO {
    private Long deliveryBoyId;
    private LocalDateTime since;
    private LocalDateTime now;
    private int count;
    private double total;
    private List<DeliveryEarning> items;

    public AdminEarningsPreviewDTO() {}
    public AdminEarningsPreviewDTO(Long deliveryBoyId, LocalDateTime since, LocalDateTime now,
                                   int count, double total, List<DeliveryEarning> items) {
        this.deliveryBoyId = deliveryBoyId; this.since = since; this.now = now;
        this.count = count; this.total = total; this.items = items;
    }
    public Long getDeliveryBoyId() { return deliveryBoyId; }
    public void setDeliveryBoyId(Long deliveryBoyId) { this.deliveryBoyId = deliveryBoyId; }
    public LocalDateTime getSince() { return since; }
    public void setSince(LocalDateTime since) { this.since = since; }
    public LocalDateTime getNow() { return now; }
    public void setNow(LocalDateTime now) { this.now = now; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public List<DeliveryEarning> getItems() { return items; }
    public void setItems(List<DeliveryEarning> items) { this.items = items; }
}
