package com.carocart.orderservice.feign;

import com.carocart.orderservice.dto.VendorDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "CaroCartAuthentication")
public interface VendorClient {
    @GetMapping("/vendors/me")
    VendorDTO getCurrentVendor(@RequestHeader("Authorization") String token);

    @GetMapping("/vendors/{id}")
    VendorDTO getVendorById(@PathVariable("id") Long id);
}
