package com.carocart.productservice.feign;

import com.carocart.productservice.dto.RatingEligibilityDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "ORDERSERVICE",                     // Eureka service name (dev)
    url = "${order.service.url:}"              // Prod override (Cloud Run URL)
)
public interface OrdersInternalClient {

    @GetMapping("/orders/internal/order-items/{orderItemId}/mini")
    RatingEligibilityDTO getMini(
        @RequestHeader("Authorization") String token,
        @PathVariable Long orderItemId
    );
}
