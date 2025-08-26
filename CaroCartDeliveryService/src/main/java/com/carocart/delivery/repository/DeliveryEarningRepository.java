package com.carocart.delivery.repository;

import com.carocart.delivery.entity.DeliveryEarning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DeliveryEarningRepository extends JpaRepository<DeliveryEarning, Long> {
    List<DeliveryEarning> findByDeliveryBoyIdOrderByDeliveredAtDesc(Long deliveryBoyId);

    List<DeliveryEarning> findByDeliveryBoyIdAndDeliveredAtBetweenOrderByDeliveredAtDesc(
            Long deliveryBoyId, LocalDateTime from, LocalDateTime to);

    boolean existsByOrderItemId(Long orderItemId);

    @Query("select coalesce(sum(e.netAmount),0) from DeliveryEarning e " +
           "where e.deliveryBoyId=:boyId and e.deliveredAt between :from and :to")
    Double sumNet(@Param("boyId") Long deliveryBoyId,
                  @Param("from") LocalDateTime from,
                  @Param("to") LocalDateTime to);

    @Query("select count(e) from DeliveryEarning e " +
           "where e.deliveryBoyId=:boyId and e.deliveredAt between :from and :to")
    Long countDelivered(@Param("boyId") Long deliveryBoyId,
                        @Param("from") LocalDateTime from,
                        @Param("to") LocalDateTime to);

    // ----- Admin payout window helpers -----

    @Query("select max(p.createdAt) from DeliveryPayout p where p.deliveryBoyId=:boyId")
    LocalDateTime findLastPayoutAt(@Param("boyId") Long deliveryBoyId);

    @Query("select e from DeliveryEarning e " +
           "where e.deliveryBoyId=:boyId and e.status='PENDING' and e.deliveredAt > :since " +
           "order by e.deliveredAt asc")
    List<DeliveryEarning> findUnpaidSince(@Param("boyId") Long deliveryBoyId,
                                          @Param("since") LocalDateTime since);
}
