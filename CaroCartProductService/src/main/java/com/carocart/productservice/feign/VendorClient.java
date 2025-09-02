package com.carocart.productservice.feign;

import com.carocart.productservice.dto.VendorResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
    name = "CAROCARTAUTHENTICATION",            // Eureka name (dev)
    url  = "${authentication.service.url:}",    // Prod override (Cloud Run URL)
    path = "/vendors"
)
public interface VendorClient {

    @GetMapping("/me")
    VendorResponseDTO getCurrentVendor(@RequestHeader("Authorization") String token);

    @GetMapping("/{id}")
    VendorResponseDTO getVendorById(@PathVariable("id") Long id);

    @GetMapping("/email/{email}")
    VendorResponseDTO getVendorByEmail(@PathVariable("email") String email);

    @GetMapping("/nearby")
    List<VendorResponseDTO> getNearbyVendors(
        @RequestParam("type") String type,
        @RequestParam("lat") double lat,
        @RequestParam("lng") double lng,
        @RequestParam("radiusKm") double radiusKm
    );

    @PostMapping("/by-ids")
    List<VendorResponseDTO> getVendorsByIds(@RequestBody List<Long> vendorIds);

    @GetMapping("/search")
    List<VendorResponseDTO> searchRestaurants(
        @RequestParam("q") String query,
        @RequestParam(value = "limit", defaultValue = "5") int limit
    );
}
