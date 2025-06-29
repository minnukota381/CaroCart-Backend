package com.carocart.orderservice.feign;

import com.carocart.orderservice.dto.EmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "CaroCartNotifications", url = "${carocart.notification.url}")
public interface NotificationClient {
    @PostMapping("/notifications/send")
    void sendEmail(@RequestBody EmailRequest request);
}
