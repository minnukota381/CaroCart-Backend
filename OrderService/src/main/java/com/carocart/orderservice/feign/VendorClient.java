package com.carocart.orderservice.feign;

import com.carocart.orderservice.dto.VendorResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "CAROCARTAUTHENTICATION",           // Local (Eureka)
    url  = "${authentication.service.url:}",   // Prod (Cloud Run)
    path = "/vendors",
    contextId = "vendorClient"
)
public interface VendorClient {

    @GetMapping("/me")
    VendorResponseDTO getCurrentVendor(@RequestHeader("Authorization") String token);

    @GetMapping("/{id}")
    VendorResponseDTO getVendorById(
        @RequestHeader("Authorization") String token,
        @PathVariable("id") Long id
    );

    @GetMapping("/email/{email}")
    VendorResponseDTO getVendorByEmail(@PathVariable("email") String email);
}
