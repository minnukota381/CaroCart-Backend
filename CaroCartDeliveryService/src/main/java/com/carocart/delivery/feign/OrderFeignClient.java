package com.carocart.delivery.feign;

import com.carocart.delivery.dto.DeliveryOrderDTO;
import com.carocart.delivery.dto.DeliveryStatusUpdateDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
	    name = "order-service",
	      // Use this property from application.properties
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
	    @PostMapping("/orders/{orderId}/out-for-delivery")
	    DeliveryOrderDTO markOrderOutForDelivery(@PathVariable("orderId") Long orderId,
	                                             @RequestHeader("Authorization") String token);

	    @PostMapping("/orders/{orderId}/deliver")
	    void markOrderAsDelivered(@PathVariable("orderId") Long orderId,
	                              @RequestBody DeliveryStatusUpdateDTO dto,
	                              @RequestHeader("Authorization") String token);
	}
