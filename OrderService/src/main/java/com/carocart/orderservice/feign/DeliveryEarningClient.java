package com.carocart.orderservice.feign;

import com.carocart.orderservice.dto.DeliveryEarningCreateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "CAROCARTDELIVERY",               // Local (Eureka)
    url  = "${delivery.service.url:}",       // Prod (Cloud Run)
    path = "/delivery/internal"              // common prefix
)
public interface DeliveryEarningClient {

    @PostMapping("/earnings")
    void create(
        @RequestHeader("Authorization") String systemBearer, // "Bearer xyz"
        @RequestBody DeliveryEarningCreateRequest req
    );
}
