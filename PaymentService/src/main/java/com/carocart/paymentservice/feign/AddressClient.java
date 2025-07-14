package com.carocart.paymentservice.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.carocart.paymentservice.dto.AddressResponseDTO;
@FeignClient(name = "ADDRESS-SERVICE")
public interface AddressClient {
    // Update the path to match the actual controller mapping!
    @GetMapping("/api/addresses/{addressId}")
    AddressResponseDTO getAddressById(@PathVariable("addressId") Long addressId);
}


