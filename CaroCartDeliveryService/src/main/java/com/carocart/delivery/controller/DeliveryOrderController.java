package com.carocart.delivery.controller;

import com.carocart.delivery.dto.DeliveryOrderDTO;
import com.carocart.delivery.service.DeliveryOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/delivery/orders")
public class DeliveryOrderController {

    @Autowired
    private DeliveryOrderService deliveryOrderService;

    // Get all available (unassigned, ready for delivery) orders
    @GetMapping("/available")
    public List<DeliveryOrderDTO> getAvailableOrders() {
        return deliveryOrderService.getAvailableOrders();
    }

    // Claim order (self-assignment)
    @PostMapping("/{orderId}/claim")
    public void claimOrder(@PathVariable Long orderId, @RequestParam Long deliveryBoyId) {
        deliveryOrderService.claimOrder(orderId, deliveryBoyId);
    }

    // Assigned orders
    @GetMapping("/assigned")
    public List<DeliveryOrderDTO> getAssignedOrders(@RequestParam Long deliveryBoyId) {
        return deliveryOrderService.getAssignedOrders(deliveryBoyId);
    }

    // Update delivery status
    @PutMapping("/{orderId}/status")
    public void updateStatus(@PathVariable Long orderId,
                             @RequestParam Long deliveryBoyId,
                             @RequestParam String status) {
        deliveryOrderService.updateDeliveryStatus(orderId, deliveryBoyId, status);
    }
}
