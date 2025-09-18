package com.carocart.orderservice.feign;

import com.carocart.orderservice.dto.DeliveryBoyDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "CaroCartDeliveryService",                 // Local (Eureka)
    url  = "${delivery.service.url:}",         // Prod (Cloud Run)
    path = "/deliveryboys"
)
public interface DeliveryBoyClient {

    @GetMapping("/me")
    DeliveryBoyDTO getCurrentDeliveryBoy(@RequestHeader("Authorization") String token);

    @GetMapping("/{id}")
    DeliveryBoyDTO getById(
        @RequestHeader("Authorization") String token,
        @PathVariable("id") Long id
    );
}

