
package com.carocart.delivery.controller;

import com.carocart.delivery.dto.DeliveryEarningCreateRequest;
import com.carocart.delivery.dto.EarningsSummaryDTO;
import com.carocart.delivery.entity.DeliveryEarning;
import com.carocart.delivery.service.DeliveryEarningService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/delivery")
public class DeliveryEarningController {

	 private final DeliveryEarningService service;

	    public DeliveryEarningController(DeliveryEarningService service) {
	        this.service = service;
	    }
	
    @PostMapping("/internal/earnings")
    public ResponseEntity<DeliveryEarning> createInternal(@RequestBody DeliveryEarningCreateRequest req) {
        return ResponseEntity.ok(service.createInternal(req));
    }

    @GetMapping("/earnings")
    public ResponseEntity<List<DeliveryEarning>> listMine(
            Authentication auth,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Long deliveryBoyId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(service.listMine(deliveryBoyId, from, to));
    }

    @GetMapping("/earnings/summary")
    public ResponseEntity<EarningsSummaryDTO> summary(
            Authentication auth, @RequestParam(defaultValue = "week") String period) {
        Long deliveryBoyId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(service.summary(deliveryBoyId, period));
    }
}