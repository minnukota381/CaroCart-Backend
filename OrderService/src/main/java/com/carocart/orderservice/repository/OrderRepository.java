package com.carocart.orderservice.repository;

import com.carocart.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Existing
    List<Order> findByUserId(Long userId);

//    // NEW: For listing ready-for-delivery orders (not assigned)
//    List<Order> findByDeliveryStatusAndDeliveryBoyIdIsNull(String deliveryStatus);
//
//    // NEW: For listing orders assigned to a delivery boy
//    List<Order> findByDeliveryBoyId(Long deliveryBoyId);
}
