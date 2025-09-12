package com.carocart.orderservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Proxies the AddressService endpoint that returns driving distance (km).
 * Adjust 'name' to your service id if you use Eureka, or set url= if direct.
 */
@FeignClient(name = "CaroCartAddressService") // or url="${ADDRESS_SERVICE_URL}"
public interface LocationClient {

    @GetMapping("/location/driving-km")
    Double drivingKm(
        @RequestParam("oLat") double oLat,
        @RequestParam("oLng") double oLng,
        @RequestParam("dLat") double dLat,
        @RequestParam("dLng") double dLng
    );
}
