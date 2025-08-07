package com.carocart.orderservice.feign;

import com.carocart.orderservice.dto.VendorResponseDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "CaroCartAuthentication")
public interface VendorClient {
    @GetMapping("/vendors/me")
    VendorResponseDTO getCurrentVendor(@RequestHeader("Authorization") String token);
    @GetMapping("/vendors/{id}")
    VendorResponseDTO getVendorById(
        @RequestHeader("Authorization") String token,
        @PathVariable("id") Long id
    );
    @GetMapping("/vendors/email/{email}")
    VendorResponseDTO getVendorByEmail(@PathVariable("email") String email);
    
}

