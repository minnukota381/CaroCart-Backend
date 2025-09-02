package com.carocart.authentication.feign;

import com.carocart.authentication.dto.EmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "CAROCARTNOTIFICATIONS",          // matches Eureka service name in dev
    url = "${notifications.service.url:}",   // empty in dev -> use Eureka; set in prod -> use URL
    path = "/notifications"                  // common prefix
)
public interface NotificationClient {
  @PostMapping("/send")
  void sendEmail(@RequestBody EmailRequest req);
}
