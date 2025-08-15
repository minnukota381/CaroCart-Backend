package com.carocart.orderservice.repository;

import com.carocart.orderservice.entity.ApprovalStatus;
import com.carocart.orderservice.entity.OrderItem;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByVendorEmailAndApprovalStatus(String vendorEmail, String approvalStatus); // Needed for old logic
    List<OrderItem> findByVendorEmailAndApprovalStatus(String vendorEmail, ApprovalStatus approvalStatus);
    List<OrderItem> findByVendorEmail(String vendorEmail);
    List<OrderItem> findByApprovalStatusAndDeliveryBoyIdIsNull(ApprovalStatus approvalStatus);
    List<OrderItem> findByDeliveryBoyId(Long deliveryBoyId);
    List<OrderItem> findByDeliveryBoyIdAndDeliveryStatus(Long deliveryBoyId, String deliveryStatus);
    List<OrderItem> findByOrderId(Long orderId);

    @Query("select o.addressId from OrderItem oi join oi.order o where oi.id = :orderItemId")
    Optional<Long> findAddressIdByOrderItemId(@Param("orderItemId") Long orderItemId);
    
    List<OrderItem> findByApprovalStatus(ApprovalStatus status);

}

