package com.carocart.delivery.service.impl;

import com.carocart.delivery.dto.DeliveryOrderItemDTO;
import com.carocart.delivery.dto.OrderItemDTO;
import com.carocart.delivery.feign.OrderFeignClient;
import com.carocart.delivery.service.DeliveryOrderService;
import com.carocart.delivery.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryOrderServiceImpl implements DeliveryOrderService {

    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private JwtUtil jwtUtil;

    private DeliveryOrderItemDTO mapToDeliveryDTO(OrderItemDTO item) {
        DeliveryOrderItemDTO dto = new DeliveryOrderItemDTO();
        dto.setOrderItemId(item.getId());
        dto.setOrderId(item.getOrderId());
        dto.setProductName(item.getProductName());
        dto.setQuantity(item.getQuantity());
        dto.setDeliveryAddress(item.getDeliveryAddress());
        dto.setUserName(item.getUserName());
        dto.setUserPhone(item.getUserPhone());
        dto.setPaymentType(item.getPaymentType());
        dto.setDeliveryStatus(item.getDeliveryStatus());
        return dto;
    }

    private String systemBearer() {
        return "Bearer " + jwtUtil.getOrGenerateInternalToken();
    }

    @Override
    public List<DeliveryOrderItemDTO> getAvailableOrderItems() {
        List<OrderItemDTO> items = orderFeignClient.getAvailableOrderItems(systemBearer());
        return items.stream().map(this::mapToDeliveryDTO).collect(Collectors.toList());
    }

    @Override
    public void assignOrderItem(Long orderItemId, Long deliveryBoyId) {
        orderFeignClient.assignOrderItemToDeliveryBoy(systemBearer(), orderItemId, deliveryBoyId);
    }

    @Override
    public List<DeliveryOrderItemDTO> getAssignedOrderItems(Long deliveryBoyId) {
        List<OrderItemDTO> items = orderFeignClient.getAssignedOrderItems(systemBearer(), deliveryBoyId);
        return items.stream().map(this::mapToDeliveryDTO).collect(Collectors.toList());
    }

    @Override
    public void updateOrderItemStatus(Long orderItemId, Long deliveryBoyId, String status) {
        orderFeignClient.updateDeliveryStatus(systemBearer(), orderItemId, status);
    }

    @Override
    public List<DeliveryOrderItemDTO> getDeliveredOrderItems(Long deliveryBoyId) {
        List<OrderItemDTO> items = orderFeignClient.getDeliveredOrderItems(systemBearer(), deliveryBoyId);
        return items.stream().map(this::mapToDeliveryDTO).collect(Collectors.toList());
    }
}
