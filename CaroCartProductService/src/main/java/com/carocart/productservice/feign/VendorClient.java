package com.carocart.productservice.feign;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.carocart.productservice.dto.VendorDTO;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "CaroCartAuthentication", url = "${carocart.auth.url}", contextId = "vendorClient")
public interface VendorClient {
    
    @GetMapping("/vendors/me")
    VendorDTO getCurrentVendor(@RequestHeader("Authorization") String token);
    @GetMapping("/vendors/{id}")
    VendorDTO getVendorById(@PathVariable("id") Long id);

}
