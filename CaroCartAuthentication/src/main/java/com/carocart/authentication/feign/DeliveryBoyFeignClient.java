package com.carocart.authentication.feign;

import com.carocart.authentication.dto.DeliveryBoyDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
	    name = "CAROCARTDELIVERYSERVICE",      // dev: Eureka
	    url  = "${delivery.service.url:}",     // prod: static URL (when set)
	    path = "/deliveryboys"
	)
	public interface DeliveryBoyFeignClient {

	    @GetMapping("/status/{status}")
	    List<DeliveryBoyDTO> getDeliveryBoysByStatus(@PathVariable String status);

	    @PutMapping("/{id}/status")
	    void updateDeliveryBoyStatus(@PathVariable Long id, @RequestParam String status);
	}
