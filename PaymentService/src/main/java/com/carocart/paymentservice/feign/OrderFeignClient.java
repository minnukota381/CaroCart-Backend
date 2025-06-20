package com.carocart.paymentservice.feign;

import com.carocart.paymentservice.dto.PaymentUpdateRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "ORDER-SERVICE", url = "http://localhost:8084/orders")
public interface OrderFeignClient {

    @GetMapping("/{orderId}")
    ResponseEntity<Map<String, Object>> getOrderById(@PathVariable("orderId") Long orderId);

    @PutMapping("/{orderId}/payment")
    ResponseEntity<String> updatePaymentStatus(@PathVariable("orderId") Long orderId,
                                               @RequestBody PaymentUpdateRequestDTO request);
}
