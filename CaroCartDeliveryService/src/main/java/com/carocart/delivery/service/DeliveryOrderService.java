package com.carocart.delivery.service;

import com.carocart.delivery.dto.DeliveryOrderDTO;

import java.util.List;

public interface DeliveryOrderService {
    List<DeliveryOrderDTO> getAvailableOrders();
    void claimOrder(Long orderId, Long deliveryBoyId);
    List<DeliveryOrderDTO> getAssignedOrders(Long deliveryBoyId);
    void updateDeliveryStatus(Long orderId, Long deliveryBoyId, String status);
}
