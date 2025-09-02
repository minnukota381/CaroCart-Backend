package com.carocart.productservice.feign;

import com.carocart.productservice.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "CAROCARTAUTHENTICATION",                 // Eureka service name (dev)
    url = "${authentication.service.url:}"           // Prod override (Cloud Run URL)
)
public interface UserClient {

    @GetMapping("/users/me")
    UserResponseDTO getCurrentUser(@RequestHeader("Authorization") String token);
}
