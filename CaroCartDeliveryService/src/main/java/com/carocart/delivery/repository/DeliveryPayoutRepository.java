package com.carocart.delivery.repository;

import com.carocart.delivery.entity.DeliveryPayout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryPayoutRepository extends JpaRepository<DeliveryPayout, Long> {
    List<DeliveryPayout> findByDeliveryBoyIdOrderByCreatedAtDesc(Long deliveryBoyId);
}
