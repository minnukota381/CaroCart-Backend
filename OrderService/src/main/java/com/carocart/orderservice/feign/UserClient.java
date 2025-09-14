package com.carocart.orderservice.feign;

import com.carocart.orderservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "CAROCARTAUTHENTICATION",           // Local (Eureka)
    url  = "${authentication.service.url:}",   // Prod (Cloud Run)
    path = "/users",
    contextId = "orderUserClient"
)
public interface UserClient {

    @GetMapping("/me")
    UserDTO getCurrentUser(@RequestHeader("Authorization") String token);

    @GetMapping("/{id}")
    UserDTO getUserById(
        @RequestHeader("Authorization") String token,
        @PathVariable("id") Long id
    );

    @GetMapping("/{id}")
    UserDTO getCurrentUserById(
        @RequestHeader("Authorization") String token,
        @PathVariable("id") Long id
    );
}
