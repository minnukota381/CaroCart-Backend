package com.carocart.delivery.repository;

import com.carocart.delivery.entity.DeliveryEarning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface DeliveryEarningRepository extends JpaRepository<DeliveryEarning, Long> {
    List<DeliveryEarning> findByDeliveryBoyIdOrderByDeliveredAtDesc(Long deliveryBoyId);

    List<DeliveryEarning> findByDeliveryBoyIdAndDeliveredAtBetweenOrderByDeliveredAtDesc(
            Long deliveryBoyId, LocalDateTime from, LocalDateTime to);

    boolean existsByOrderItemId(Long orderItemId);

    @Query("select coalesce(sum(e.netAmount),0) from DeliveryEarning e where e.deliveryBoyId=:deliveryBoyId and e.deliveredAt between :from and :to")
    Double sumNet(Long deliveryBoyId, LocalDateTime from, LocalDateTime to);

    @Query("select count(e) from DeliveryEarning e where e.deliveryBoyId=:deliveryBoyId and e.deliveredAt between :from and :to")
    Long countDelivered(Long deliveryBoyId, LocalDateTime from, LocalDateTime to);
}
