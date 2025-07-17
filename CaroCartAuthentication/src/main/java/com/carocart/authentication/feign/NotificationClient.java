package com.carocart.authentication.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.carocart.authentication.dto.EmailRequest;

@FeignClient(name = "CAROCARTNOTIFICATIONS")
public interface NotificationClient {

    @PostMapping("/notifications/send")
    void sendEmail(@RequestBody EmailRequest request);
}
