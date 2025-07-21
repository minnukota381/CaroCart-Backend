package com.carocart.orderservice.repository;

import com.carocart.orderservice.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    java.util.List<OrderItem> findByVendorEmailAndApprovalStatus(String vendorEmail, String approvalStatus);
    java.util.List<OrderItem> findByApprovalRequiredByAndApprovalStatus(String approvalRequiredBy, String approvalStatus);
}
