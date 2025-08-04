package com.carocart.delivery.service;

import com.carocart.delivery.dto.DeliveryOrderItemDTO;
import java.util.List;

public interface DeliveryOrderService {
    List<DeliveryOrderItemDTO> getAvailableOrderItems();
    void assignOrderItem(Long orderItemId, Long deliveryBoyId);
    List<DeliveryOrderItemDTO> getAssignedOrderItems(Long deliveryBoyId);
    void updateOrderItemStatus(Long orderItemId, Long deliveryBoyId, String status);
    List<DeliveryOrderItemDTO> getDeliveredOrderItems(Long deliveryBoyId);
}
