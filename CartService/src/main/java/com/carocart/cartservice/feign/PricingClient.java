package com.carocart.cartservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "OrderService", path = "/orders")
public interface PricingClient {
    @GetMapping("/delivery-fee")
    Double getDeliveryFee(@RequestParam("vendorId") Long vendorId,
                          @RequestParam("subtotal") Double subtotal,
                          @RequestParam("userLat") Double userLat,
                          @RequestParam("userLng") Double userLng);
}
