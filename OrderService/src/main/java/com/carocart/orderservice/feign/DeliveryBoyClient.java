package com.carocart.orderservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.carocart.orderservice.dto.DeliveryBoyDTO;

@FeignClient(name = "CaroCartDeliveryService")
public interface DeliveryBoyClient {
    @GetMapping("/deliveryboys/me")
    DeliveryBoyDTO getCurrentDeliveryBoy(@RequestHeader("Authorization") String token);


    @GetMapping("/deliveryboys/{id}")
    DeliveryBoyDTO getById(@RequestHeader("Authorization") String token, @PathVariable Long id);

}
