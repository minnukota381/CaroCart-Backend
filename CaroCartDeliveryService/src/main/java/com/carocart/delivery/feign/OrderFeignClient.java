package com.carocart.delivery.feign;

import com.carocart.delivery.dto.OrderItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "OrderService")
public interface OrderFeignClient {
    @GetMapping("/orders/orderitems/available-for-delivery")
    List<OrderItemDTO> getAvailableOrderItems();

    @PutMapping("/orders/orderitems/{orderItemId}/assign-delivery")
    void assignOrderItemToDeliveryBoy(@PathVariable Long orderItemId, @RequestParam Long deliveryBoyId);

    @GetMapping("/orders/orderitems/assigned/{deliveryBoyId}")
    List<OrderItemDTO> getAssignedOrderItems(@PathVariable Long deliveryBoyId);

    @PutMapping("/orders/orderitems/{orderItemId}/delivery-status")
    void updateDeliveryStatus(@PathVariable Long orderItemId, @RequestParam String status);

    @GetMapping("/orders/orderitems/delivered/{deliveryBoyId}")
    List<OrderItemDTO> getDeliveredOrderItems(@PathVariable Long deliveryBoyId);
}
