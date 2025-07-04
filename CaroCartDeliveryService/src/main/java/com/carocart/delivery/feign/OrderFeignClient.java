package com.carocart.delivery.feign;

import com.carocart.delivery.dto.DeliveryOrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
	    name = "order-service",
	    url = "${order.service.url}",  // Use this property from application.properties
	    configuration = com.carocart.delivery.config.FeignClientConfig.class
	)
	public interface OrderFeignClient {
	    @GetMapping("/orders/ready-for-delivery")
	    List<DeliveryOrderDTO> getReadyForDeliveryOrders();

	    @PutMapping("/orders/{orderId}/assign-delivery")
	    void assignOrderToDeliveryBoy(@PathVariable Long orderId, @RequestParam Long deliveryBoyId);

	    @GetMapping("/orders/assigned/{deliveryBoyId}")
	    List<DeliveryOrderDTO> getAssignedOrders(@PathVariable Long deliveryBoyId);

	    @PutMapping("/orders/{orderId}/delivery-status")
	    void updateDeliveryStatus(@PathVariable Long orderId, @RequestParam String status);
	}
