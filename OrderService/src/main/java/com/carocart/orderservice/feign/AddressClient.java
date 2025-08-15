// package: com.carocart.orderservice.feign

package com.carocart.orderservice.feign;

import com.carocart.orderservice.dto.AddressDTO;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "CaroCartAddress")
public interface AddressClient {

    @GetMapping("/api/addresses/{id}")
    AddressDTO getAddressById(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);

    @GetMapping("/api/addresses/me")
    List<AddressDTO> getMyAddresses(@RequestHeader("Authorization") String token);
    @GetMapping("/api/addresses/{id}")
    AddressDTO getAddressById(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long id
    );
    @GetMapping("/api/addresses/user/{userId}")
    List<AddressDTO> getAllAddressesByUserId(@PathVariable Long userId);
}
