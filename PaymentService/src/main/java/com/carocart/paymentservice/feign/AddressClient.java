package com.carocart.paymentservice.feign;

import com.carocart.paymentservice.dto.AddressResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "CAROCARTADDRESS",               // Local (Eureka)
    url  = "${address.service.url:}",       // Prod (Cloud Run)
    path = "/api/addresses",
    contextId = "paymentAddressClient"
)
public interface AddressClient {

    @GetMapping("/{addressId}")
    AddressResponseDTO getAddressById(@PathVariable("addressId") Long addressId);
}
