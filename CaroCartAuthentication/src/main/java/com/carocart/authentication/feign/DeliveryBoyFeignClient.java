package com.carocart.authentication.feign;

import com.carocart.authentication.dto.DeliveryBoyDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "CAROCARTDELIVERYSERVICE") // service name registered in Eureka for delivery
public interface DeliveryBoyFeignClient {

    @GetMapping("/deliveryboys/status/{status}")
    List<DeliveryBoyDTO> getDeliveryBoysByStatus(@PathVariable String status);

    @PutMapping("/deliveryboys/{id}/status")
    void updateDeliveryBoyStatus(@PathVariable Long id, @RequestParam String status);
}
