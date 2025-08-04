package com.carocart.delivery.feign;

import com.carocart.delivery.dto.EmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "CAROCARTNOTIFICATIONS")
public interface NotificationClient {

    @PostMapping("/send-email")
    void sendEmail(EmailRequest emailRequest);
}
