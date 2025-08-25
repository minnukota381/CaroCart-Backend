package com.carocart.delivery.feign;


import com.carocart.delivery.dto.OrderResponse; // create a mirror DTO or reuse via shared lib
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@FeignClient(name = "OrderService")
public interface OrderFeignClient {


@GetMapping("/orders/delivery/available")
List<OrderResponse> getAvailableOrders(@RequestHeader("Authorization") String token);


@PutMapping("/orders/delivery/assign/{orderId}")
void assign(@RequestHeader("Authorization") String token, @PathVariable Long orderId);


@PutMapping("/orders/delivery/update-status/{orderId}")
void updateStatus(@RequestHeader("Authorization") String token,
@PathVariable Long orderId,
@RequestParam String status);


@GetMapping("/orders/my-assigned")
List<OrderResponse> myAssigned(@RequestHeader("Authorization") String token);
}