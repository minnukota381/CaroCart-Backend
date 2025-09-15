package com.carocart.orderservice.feign;

import com.carocart.orderservice.dto.EmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "CAROCARTNOTIFICATIONS",            // Local (Eureka)
    url  = "${notification.service.url:}",     // Prod (Cloud Run)
    path = "/notifications"
)
public interface NotificationClient {

    @PostMapping(value = "/email", consumes = "application/json")
    void sendEmail(@RequestBody EmailRequest request);
}
