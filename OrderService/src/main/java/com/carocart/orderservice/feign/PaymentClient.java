// orderservice/src/main/java/com/carocart/orderservice/feign/PaymentClient.java
package com.carocart.orderservice.feign;

import com.carocart.orderservice.dto.CreateVendorPayoutRequest;
import com.carocart.orderservice.dto.VendorPayoutResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "PaymentService")
public interface PaymentClient {
  @PostMapping("/internal/vendor-payouts")
  VendorPayoutResponseDTO createVendorPayout(
      @RequestHeader("Authorization") String internalToken,
      @RequestBody CreateVendorPayoutRequest body
  );
}
