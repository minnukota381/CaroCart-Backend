package com.carocart.delivery.repository;

import com.carocart.delivery.entity.DeliveryBoy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryBoyRepository extends JpaRepository<DeliveryBoy, Long> {
    Optional<DeliveryBoy> findByPhone(String phone);
    Optional<DeliveryBoy> findByEmail(String email);
}
