package com.carocart.orderservice.feign;

import com.carocart.orderservice.dto.DeliveryEarningCreateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "CaroCartDeliveryService")
public interface DeliveryEarningClient {
    @PostMapping("/delivery/internal/earnings")
    void create(@RequestHeader("Authorization") String systemBearer,
                @RequestBody DeliveryEarningCreateRequest req);
}
