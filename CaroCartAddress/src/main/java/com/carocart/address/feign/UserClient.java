// com/carocart/address/feign/UserClient.java
package com.carocart.address.feign;

import com.carocart.address.DTO.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "CAROCARTAUTHENTICATION",            // Eureka in dev
    url  = "${authentication.service.url:}",    // Cloud Run URL in prod
    contextId = "addressUserClient",
    path = "/users"
)
public interface UserClient {

    @GetMapping("/me")
    UserResponseDTO getCurrentUser(@RequestHeader("Authorization") String token);
}
