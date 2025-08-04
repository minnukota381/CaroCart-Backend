package com.carocart.delivery.repository;

import com.carocart.delivery.entity.DeliveryBoy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryBoyRepository extends JpaRepository<DeliveryBoy, Long> {
	
    Optional<DeliveryBoy> findByPhone(String phone);
    Optional<DeliveryBoy> findByEmail(String email);
    List<DeliveryBoy> findByStatus(String status);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email); // <-- Make sure it's here!
}

