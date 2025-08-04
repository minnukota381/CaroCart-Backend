package com.carocart.delivery.controller;

import com.carocart.delivery.dto.DeliveryOrderItemDTO;
import com.carocart.delivery.service.DeliveryOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/delivery/orders")
public class DeliveryOrderController {

    @Autowired
    private DeliveryOrderService deliveryOrderService;

    @GetMapping("/available")
    public List<DeliveryOrderItemDTO> getAvailableOrderItems() {
        return deliveryOrderService.getAvailableOrderItems();
    }

    @PutMapping("/assign/{orderItemId}")
    public ResponseEntity<String> assignOrderItem(
            @PathVariable Long orderItemId,
            Authentication authentication
    ) {
        Long deliveryBoyId = (Long) authentication.getPrincipal();
        deliveryOrderService.assignOrderItem(orderItemId, deliveryBoyId);
        return ResponseEntity.ok("Order item assigned to you");
    }

    @GetMapping("/assigned")
    public List<DeliveryOrderItemDTO> getAssignedOrderItems(Authentication authentication) {
        Long deliveryBoyId = (Long) authentication.getPrincipal();
        return deliveryOrderService.getAssignedOrderItems(deliveryBoyId);
    }

    @PutMapping("/{orderItemId}/status")
    public ResponseEntity<String> updateOrderItemStatus(
            @PathVariable Long orderItemId,
            @RequestParam String status,
            Authentication authentication
    ) {
        Long deliveryBoyId = (Long) authentication.getPrincipal();
        deliveryOrderService.updateOrderItemStatus(orderItemId, deliveryBoyId, status);
        return ResponseEntity.ok("Order item status updated");
    }

    @GetMapping("/history")
    public List<DeliveryOrderItemDTO> getDeliveredOrderItems(Authentication authentication) {
        Long deliveryBoyId = (Long) authentication.getPrincipal();
        return deliveryOrderService.getDeliveredOrderItems(deliveryBoyId);
    }
}
