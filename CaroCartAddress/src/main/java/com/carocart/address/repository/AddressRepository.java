package com.carocart.address.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carocart.address.entity.Address;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    
    Optional<Address> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("update Address a set a.isDefault = false where a.userId = :userId")
    void clearDefaultForUser(@Param("userId") Long userId);
}
