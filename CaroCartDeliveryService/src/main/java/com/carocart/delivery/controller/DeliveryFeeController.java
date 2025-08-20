// DeliveryFeeController.java
package com.carocart.delivery.controller;

import com.carocart.delivery.dto.DeliveryFeeRequest;
import com.carocart.delivery.dto.DeliveryFeeResponse;
import com.carocart.delivery.service.DeliveryFeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/delivery/fee")
public class DeliveryFeeController {

    @Autowired
    private DeliveryFeeService deliveryFeeService;

    @PostMapping("/calculate")
    public ResponseEntity<DeliveryFeeResponse> calculateFee(@RequestBody DeliveryFeeRequest request) {
        return ResponseEntity.ok(deliveryFeeService.calculateFee(request));
    }
}
