package com.carocart.productservice.feign;

import com.carocart.productservice.dto.VendorResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "carocartauthentication") // Ensure this matches your auth microservice app name
public interface VendorClient {
    @GetMapping("/vendors/me")
    VendorResponseDTO getCurrentVendor(@RequestHeader("Authorization") String token);

    @GetMapping("/vendors/{id}")
    VendorResponseDTO getVendorById(@PathVariable("id") Long id);

    @GetMapping("/vendors/email/{email}")
    VendorResponseDTO getVendorByEmail(@PathVariable("email") String email);

    @GetMapping("/vendors/nearby")
    List<VendorResponseDTO> getNearbyVendors(
        @RequestParam("type") String type,
        @RequestParam("lat") double lat,
        @RequestParam("lng") double lng,
        @RequestParam("radiusKm") double radiusKm
    );
}
