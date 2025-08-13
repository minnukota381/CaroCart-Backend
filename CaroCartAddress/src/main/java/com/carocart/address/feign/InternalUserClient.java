// com/carocart/address/feign/InternalUserClient.java
package com.carocart.address.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name="CaroCartAuthentication", contextId="addressInternalUserClient")
public interface InternalUserClient {
  @PatchMapping("/users/internal/{id}/location")
  void updateUserLocation(@RequestHeader("Authorization") String token,
                          @PathVariable("id") Long userId,
                          @RequestBody Map<String, Double> body);
}



