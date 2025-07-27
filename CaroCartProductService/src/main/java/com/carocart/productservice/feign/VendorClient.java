package com.carocart.productservice.feign;

import com.carocart.productservice.dto.VendorDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;  // <-- Make sure to import this!

@FeignClient(name = "CaroCartAuthentication")
public interface VendorClient {
    @GetMapping("/vendors/me")
    VendorDTO getCurrentVendor(@RequestHeader("Authorization") String token);

    @GetMapping("/vendors/{id}")
    VendorDTO getVendorById(@PathVariable("id") Long id);

    // --- New: Get nearby vendors (for food/groceries by geo) ---
    @GetMapping("/vendors/nearby")
    List<VendorDTO> getNearbyVendors(
        @RequestParam("type") String type,         // "FOOD" or "GROCERY"
        @RequestParam("lat") double lat,
        @RequestParam("lng") double lng,
        @RequestParam("radiusKm") double radiusKm  // usually 10
    );
}
