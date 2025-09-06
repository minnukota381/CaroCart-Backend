package com.carocart.orderservice.repository;

import com.carocart.orderservice.entity.Order;
import com.carocart.orderservice.entity.VendorSettlementStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);
    List<Order> findByStatusAndOrderDateBetween(String status, LocalDateTime start, LocalDateTime end);
    List<Order> findByVendorId(Long vendorId);
    List<Order> findByVendorIdAndStatus(Long vendorId, String status);
    long countByUserId(Long userId);
    long countByUserIdAndStatusIn(Long userId, Collection<String> statuses);

    @Query("""
      select o from Order o
       where o.vendorId = :vendorId
         and o.status = 'DELIVERED'
         and o.vendorSettlementStatus = :status
         and o.deliveredAt > :since
         and o.deliveredAt <= :to
       order by o.deliveredAt asc
    """)
    List<Order> findDeliveredUnpaidSince(@Param("vendorId") Long vendorId,
                                         @Param("since") LocalDateTime since,
                                         @Param("to") LocalDateTime to,
                                         @Param("status") VendorSettlementStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      update Order o
         set o.vendorSettlementStatus = :paid,
             o.vendorPayoutId = :payoutId
       where o.id in :orderIds
    """)
    int markOrdersPaid(@Param("orderIds") List<Long> orderIds,
                       @Param("payoutId") String payoutId,
                       @Param("paid") VendorSettlementStatus paid);

    @Query("""
      select max(o.deliveredAt)
        from Order o
       where o.vendorId = :vendorId
         and o.vendorSettlementStatus = :paid
    """)
    LocalDateTime findMaxDeliveredAtWherePaid(@Param("vendorId") Long vendorId,
                                              @Param("paid") VendorSettlementStatus paid);
}
