package com.carocart.productservice.feign;

import com.carocart.productservice.dto.AdminResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "CAROCARTAUTHENTICATION",                 // Eureka name (dev)
    url = "${authentication.service.url:}"           // Prod override (Cloud Run URL)
)
public interface AdminClient {

    @GetMapping("/admins/me")
    AdminResponseDTO getCurrentAdmin(@RequestHeader("Authorization") String token);
}
