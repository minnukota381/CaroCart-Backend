package com.carocart.orderservice.feign;

import com.carocart.orderservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
@FeignClient(name = "CaroCartAuthentication")
public interface UserClient {

    @GetMapping("/users/me")
    UserDTO getCurrentUser(@RequestHeader("Authorization") String token);

    @GetMapping("/users/{id}")
    UserDTO getCurrentUserById(@RequestHeader("Authorization") String token, @PathVariable("id") Long id);
}


