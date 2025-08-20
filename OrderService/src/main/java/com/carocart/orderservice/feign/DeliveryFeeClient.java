package com.carocart.orderservice.feign;

import com.carocart.orderservice.dto.DeliveryFeeRequest;
import com.carocart.orderservice.dto.DeliveryFeeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "DeliveryService")
public interface DeliveryFeeClient {

    @PostMapping("/delivery/fee/calculate")
    DeliveryFeeResponse calculateDeliveryFee(@RequestBody DeliveryFeeRequest request);
}
