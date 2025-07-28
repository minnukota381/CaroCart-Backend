package com.carocart.delivery.service.impl;

import com.carocart.delivery.dto.DeliveryOrderDTO;
import com.carocart.delivery.dto.DeliveryStatusUpdateDTO;
import com.carocart.delivery.feign.OrderFeignClient;
import com.carocart.delivery.service.DeliveryOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeliveryOrderServiceImpl implements DeliveryOrderService {
// (define this JPA repo in your delivery service or call via Feign)

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
    
    @Override
    public DeliveryOrderDTO markOrderOutForDelivery(Long orderId, String deliveryBoyToken) {
        return orderFeignClient.markOrderOutForDelivery(orderId, deliveryBoyToken);
    }

    @Override
    public void markOrderAsDelivered(Long orderId, String otp, String deliveryBoyToken) {
        DeliveryStatusUpdateDTO dto = new DeliveryStatusUpdateDTO();
        dto.setOrderId(orderId);
        dto.setOtp(otp);
        orderFeignClient.markOrderAsDelivered(orderId, dto, deliveryBoyToken);
    }

}
