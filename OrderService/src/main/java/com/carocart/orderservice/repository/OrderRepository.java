package com.carocart.orderservice.repository;

import com.carocart.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Existing
    List<Order> findByUserId(Long userId);
    List<Order> findByStatusAndOrderDateBetween(String status, LocalDateTime start, LocalDateTime end);
    List<Order> findByVendorId(Long vendorId);
    List<Order> findByVendorIdAndStatus(Long vendorId, String status);
    long countByUserId(Long userId);
    long countByUserIdAndStatusIn(Long userId, Collection<String> statuses);

}
