package com.carocart.delivery.service;

import com.carocart.delivery.dto.DeliveryOrderItemDTO;
import java.util.List;

public interface DeliveryOrderService {
    // Available orders (one per ORDER)
    List<DeliveryOrderItemDTO> getAvailableOrderItems();

    // Assign the whole ORDER to the current rider (deliveryBoyId is no longer needed here)
    void assignOrderItem(Long orderId, Long ignoredDeliveryBoyId);

    // Orders assigned to current rider (cards per ORDER)
    List<DeliveryOrderItemDTO> getAssignedOrderItems(Long deliveryBoyId);

    // Update ORDER delivery status (ASSIGNED -> PICKED_UP -> OUT_FOR_DELIVERY -> DELIVERED)
    void updateOrderItemStatus(Long orderId, Long ignoredDeliveryBoyId, String status);

    // Delivered history (optional: filter from myAssigned)
    List<DeliveryOrderItemDTO> getDeliveredOrderItems(Long deliveryBoyId);
}