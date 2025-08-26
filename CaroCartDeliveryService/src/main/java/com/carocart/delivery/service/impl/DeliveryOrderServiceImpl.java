package com.carocart.delivery.service.impl;

import com.carocart.delivery.dto.DeliveryOrderItemDTO;
import com.carocart.delivery.dto.OrderResponse;   // mirror of orderservice OrderResponse
import com.carocart.delivery.feign.OrderFeignClient;
import com.carocart.delivery.service.DeliveryOrderService;
import com.carocart.delivery.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeliveryOrderServiceImpl implements DeliveryOrderService {

    @Autowired private OrderFeignClient orderFeignClient;
    @Autowired private JwtUtil jwtUtil;

    private String bearer() { return "Bearer " + jwtUtil.getOrGenerateInternalToken(); }

    private DeliveryOrderItemDTO mapOrderToCard(OrderResponse o) {
        DeliveryOrderItemDTO d = new DeliveryOrderItemDTO();
        d.setOrderId(o.getOrderId());

        // Use enriched order-level fields (see orderservice changes below)
        d.setProductName(o.getProductName());
        d.setQuantity(o.getQuantity());

        d.setDeliveryStatus(o.getDeliveryStatus());
        d.setPaymentType(o.getPaymentStatus());
        d.setDeliveryAddress(o.getShippingAddress());

        d.setUserName(o.getUserName());
        d.setUserPhone(o.getUserPhone());

        d.setVendorLat(o.getVendorLat());
        d.setVendorLng(o.getVendorLng());
        d.setVendorPhone(o.getVendorPhone());
        return d;
    }

    @Override
    public List<DeliveryOrderItemDTO> getAvailableOrderItems() {
        return orderFeignClient.getAvailableOrders(bearer())
                .stream().map(this::mapOrderToCard).toList();
    }

    @Override
    public void assignOrderItem(Long orderId, Long deliveryBoyIdIgnored) {
        orderFeignClient.assign(bearer(), orderId); // order-level assignment
    }

    @Override
    public List<DeliveryOrderItemDTO> getAssignedOrderItems(Long deliveryBoyIdIgnored) {
        return orderFeignClient.myAssigned(bearer())
                .stream().map(this::mapOrderToCard).toList();
    }

    @Override
    public void updateOrderItemStatus(Long orderId, Long deliveryBoyIdIgnored, String status) {
        orderFeignClient.updateStatus(bearer(), orderId, status); // order-level status update
    }

    @Override
    public List<DeliveryOrderItemDTO> getDeliveredOrderItems(Long ignored) {
        return orderFeignClient.myAssigned(bearer()).stream()
                .filter(o -> "DELIVERED".equalsIgnoreCase(o.getDeliveryStatus()))
                .map(this::mapOrderToCard)
                .toList();
    }
}
