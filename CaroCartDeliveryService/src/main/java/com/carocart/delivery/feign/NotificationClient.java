package com.carocart.delivery.feign;

import com.carocart.delivery.dto.EmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "CAROCARTNOTIFICATIONS",            // Local (Eureka)
    url  = "${notification.service.url:}",     // Prod (Cloud Run)
    path = "/notifications"
)
public interface NotificationClient {

    @PostMapping("/send-email")
    void sendEmail(@RequestBody EmailRequest emailRequest);
}
