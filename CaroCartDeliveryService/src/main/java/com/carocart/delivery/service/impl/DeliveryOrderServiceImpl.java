package com.carocart.delivery.service.impl;

import com.carocart.delivery.dto.DeliveryOrderDTO;
import com.carocart.delivery.feign.OrderFeignClient;
import com.carocart.delivery.service.DeliveryOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeliveryOrderServiceImpl implements DeliveryOrderService {

    @Autowired
    private OrderFeignClient orderFeignClient;

    @Override
    public List<DeliveryOrderDTO> getAvailableOrders() {
        return orderFeignClient.getReadyForDeliveryOrders();
    }

    @Override
    public void claimOrder(Long orderId, Long deliveryBoyId) {
        orderFeignClient.assignOrderToDeliveryBoy(orderId, deliveryBoyId);
    }

    @Override
    public List<DeliveryOrderDTO> getAssignedOrders(Long deliveryBoyId) {
        return orderFeignClient.getAssignedOrders(deliveryBoyId);
    }

    @Override
    public void updateDeliveryStatus(Long orderId, Long deliveryBoyId, String status) {
        orderFeignClient.updateDeliveryStatus(orderId, status);
    }
}
