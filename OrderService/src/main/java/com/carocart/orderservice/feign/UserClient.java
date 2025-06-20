// package: com.carocart.orderservice.feign

package com.carocart.orderservice.feign;

import com.carocart.orderservice.dto.UserDTO;
import com.carocart.orderservice.dto.AddressDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "carocart-authentication", url = "http://localhost:8081", contextId = "userClient")
public interface UserClient {

    @GetMapping("/users/me")
    UserDTO getCurrentUser(@RequestHeader("Authorization") String token);
}
