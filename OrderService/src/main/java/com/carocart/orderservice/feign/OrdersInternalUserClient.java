// com/carocart/orderservice/feign/OrdersInternalUserClient.java
package com.carocart.orderservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name = "CaroCartAuthentication", contextId = "ordersInternalUserClient")
public interface OrdersInternalUserClient {
    @PatchMapping("/users/internal/{id}/location") // ✅ Fix path
    void updateUserLocation(@RequestHeader("Authorization") String internalToken,
                            @PathVariable("id") Long userId,
                            @RequestBody Map<String, Double> body);
}

