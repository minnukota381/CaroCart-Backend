package com.carocart.paymentservice.repository;

import com.carocart.paymentservice.entity.VendorPayout;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface VendorPayoutRepository extends JpaRepository<VendorPayout, String> {
  Optional<VendorPayout> findByVendorIdAndFromAtAndToAt(Long vendorId, Instant fromAt, Instant toAt);
  Optional<VendorPayout> findTopByVendorIdOrderByToAtDesc(Long vendorId);
  List<VendorPayout> findByVendorIdOrderByCreatedAtDesc(Long vendorId, Pageable pageable);
}
