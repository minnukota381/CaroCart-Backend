package com.carocart.authentication.controller;


import com.carocart.authentication.dto.DeliveryBoyDTO;
import com.carocart.authentication.service.DeliveryBoyService;  // You can expose this via Feign if services are separate
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admins/deliveryboys")
@PreAuthorize("hasRole('ADMIN')")  // Only admin role can access these APIs
public class AdminDeliveryBoyController {

    @Autowired
    private DeliveryBoyService deliveryBoyService;

    // List all delivery boys by status (mostly for pending)
    @GetMapping("/status/{status}")
    public List<DeliveryBoyDTO> getDeliveryBoysByStatus(@PathVariable String status) {
        return deliveryBoyService.getDeliveryBoysByStatus(status.toUpperCase());
    }

    // Approve or reject a delivery boy
    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateDeliveryBoyStatus(@PathVariable Long id,
                                                          @RequestParam String status) {
        if (!status.equalsIgnoreCase("APPROVED") && !status.equalsIgnoreCase("REJECTED")) {
            return ResponseEntity.badRequest().body("Invalid status. Use APPROVED or REJECTED.");
        }
        deliveryBoyService.updateDeliveryBoyStatus(id, status.toUpperCase());
        return ResponseEntity.ok("Delivery boy status updated to " + status);
    }
}
