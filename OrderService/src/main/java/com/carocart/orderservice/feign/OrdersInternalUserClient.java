package com.carocart.orderservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(
    name = "CAROCARTAUTHENTICATION",                 // Local (Eureka)
    url  = "${authentication.service.url:}",         // Prod (Cloud Run)
    path = "/users/internal",
    contextId = "ordersInternalUserClient"
)
public interface OrdersInternalUserClient {

    @PatchMapping("/{id}/location")
    void updateUserLocation(
        @RequestHeader("Authorization") String internalToken,
        @PathVariable("id") Long userId,
        @RequestBody Map<String, Double> body
    );
}
