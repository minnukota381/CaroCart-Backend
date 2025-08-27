// com/carocart/orderservice/feign/NotificationClient.java
package com.carocart.orderservice.feign;

import com.carocart.orderservice.dto.EmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Use the Eureka service ID (matches your gateway route id CAROCARTNOTIFICATIONS)
@FeignClient(name = "CaroCartNotifications")
public interface NotificationClient {

    // Use the HTML-aware endpoint
    @PostMapping(value = "/notifications/email", consumes = "application/json")
    void sendEmail(@RequestBody EmailRequest request);
}
