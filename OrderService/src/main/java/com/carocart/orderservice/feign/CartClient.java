package com.carocart.orderservice.feign;

import com.carocart.orderservice.dto.CartItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
    name = "CAROCARTCART",                // Local (Eureka)
    url  = "${cart.service.url:}",        // Prod (Cloud Run)
    path = "/cart"
)
public interface CartClient {

    @GetMapping("/get") 
    List<CartItemDTO> getUserCart(@RequestHeader("Authorization") String token);

    @DeleteMapping("/clear")
    void clearCart(@RequestHeader("Authorization") String token);
}
