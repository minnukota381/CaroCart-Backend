package com.carocart.cartservice.feign;

import com.carocart.cartservice.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "CAROCARTAUTHENTICATION",       // Dev: Eureka
    url  = "${authentication.service.url:}", // Prod: Cloud Run URL
    path = "/users"
)
public interface UserClient {

    @GetMapping("/me")
    UserResponseDTO getCurrentUser(@RequestHeader("Authorization") String token);

    @GetMapping("/{userId}")
    UserResponseDTO getUserById(@PathVariable("userId") Long userId);
}
