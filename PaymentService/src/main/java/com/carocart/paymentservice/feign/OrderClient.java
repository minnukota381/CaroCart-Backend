package com.carocart.paymentservice.feign;

import com.carocart.paymentservice.config.FeignConfig;
import com.carocart.paymentservice.dto.OrderRequestDTO;
import com.carocart.paymentservice.dto.OrderResponseDTO;
import com.carocart.paymentservice.dto.PaymentUpdateRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "OrderService", configuration = FeignConfig.class)
public interface OrderClient {
    @PostMapping("/orders/internal/place")
    ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO orderRequest);
//
//
//    @PostMapping("/orders")
//    ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO orderRequest);

    @GetMapping("/orders/{orderId}")
    ResponseEntity<Map<String, Object>> getOrderById(@PathVariable("orderId") Long orderId);

    @PutMapping("/orders/update-payment-status/{orderId}")
    ResponseEntity<?> updatePaymentStatus(
        @PathVariable("orderId") Long orderId,
        @RequestBody PaymentUpdateRequestDTO request);
}
