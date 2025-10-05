package com.carocart.orderservice.feign;

import com.carocart.orderservice.dto.CreateVendorPayoutRequest;
import com.carocart.orderservice.dto.VendorPayoutResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "PAYMENTSERVICE",                // Local (Eureka)
    url  = "${payment.service.url:}",        // Prod (Cloud Run)
    path = "/internal",
    contextId = "paymentClient"
)
public interface PaymentClient {

    @PostMapping("/vendor-payouts")
    VendorPayoutResponseDTO createVendorPayout(
        @RequestHeader("Authorization") String internalToken,
        @RequestBody CreateVendorPayoutRequest body
    );
}
