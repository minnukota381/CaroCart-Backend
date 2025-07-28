package com.carocart.delivery.controller;

import com.carocart.delivery.dto.DeliveryOrderDTO;
import com.carocart.delivery.dto.DeliveryStatusUpdateDTO;
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
    public List<DeliveryOrderDTO> getAvailableOrders() {
        return deliveryOrderService.getAvailableOrders();
    }

    @PostMapping("/{orderId}/claim")
    public void claimOrder(@PathVariable Long orderId, Authentication authentication) {
        Long deliveryBoyId = (Long) authentication.getPrincipal();
        deliveryOrderService.claimOrder(orderId, deliveryBoyId);
    }

    @GetMapping("/assigned")
    public List<DeliveryOrderDTO> getAssignedOrders(Authentication authentication) {
        Long deliveryBoyId = (Long) authentication.getPrincipal();
        return deliveryOrderService.getAssignedOrders(deliveryBoyId);
    }

    @PutMapping("/{orderId}/status")
    public void updateStatus(@PathVariable Long orderId,
                             @RequestParam String status,
                             Authentication authentication) {
        Long deliveryBoyId = (Long) authentication.getPrincipal();
        deliveryOrderService.updateDeliveryStatus(orderId, deliveryBoyId, status);
    }
    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<?> deliverOrder(
        @PathVariable Long orderId,
        @RequestBody DeliveryStatusUpdateDTO dto,
        @RequestHeader("Authorization") String token
    ) {
        deliveryOrderService.markOrderAsDelivered(orderId, dto.getOtp(), token);
        return ResponseEntity.ok("Order delivered successfully.");
    }
    @PostMapping("/{orderId}/out-for-delivery")
    public ResponseEntity<?> outForDelivery(
        @PathVariable Long orderId,
        @RequestHeader("Authorization") String token
    ) {
        DeliveryOrderDTO dto = deliveryOrderService.markOrderOutForDelivery(orderId, token);
        return ResponseEntity.ok(dto);
    }


}
