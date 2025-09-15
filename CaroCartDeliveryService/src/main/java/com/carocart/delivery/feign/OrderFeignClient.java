package com.carocart.delivery.feign;

import com.carocart.delivery.dto.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
    name = "CAROCARTORDER",                 // Local (Eureka)
    url  = "${order.service.url:}",         // Prod (Cloud Run)
    path = "/orders"
)
public interface OrderFeignClient {

    @GetMapping("/delivery/available")
    List<OrderResponse> getAvailableOrders(@RequestHeader("Authorization") String token);

    @PutMapping("/delivery/assign/{orderId}")
    void assign(
        @RequestHeader("Authorization") String token,
        @PathVariable("orderId") Long orderId
    );

    @PutMapping("/delivery/update-status/{orderId}")
    void updateStatus(
        @RequestHeader("Authorization") String token,
        @PathVariable("orderId") Long orderId,
        @RequestParam("status") String status
    );

    @GetMapping("/my-assigned")
    List<OrderResponse> myAssigned(@RequestHeader("Authorization") String token);
}
