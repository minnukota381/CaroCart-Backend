package com.carocart.productservice.feign;

import com.carocart.productservice.dto.RatingEligibilityDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "OrderService")
public interface OrdersInternalClient {
    @GetMapping("/orders/internal/order-items/{orderItemId}/mini")
    RatingEligibilityDTO getMini(@RequestHeader("Authorization") String token,
                                 @PathVariable Long orderItemId);
}
