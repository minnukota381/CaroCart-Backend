package com.carocart.authentication.repository;

import com.carocart.authentication.entity.Vendor;
import com.carocart.authentication.entity.VendorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {

    Optional<Vendor> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Vendor> findByIsApprovedFalse();

    // Find all vendors by type and approval status (for Java-side filtering, MVP)
    List<Vendor> findByVendorTypeAndIsApproved(VendorType vendorType, boolean isApproved);

    // ----- Optional: Native Spatial Query (for MySQL/PostGIS, production scale) -----
    // Uncomment and use this if/when your DB supports it
    /*
    @Query(
        value = "SELECT *, " +
                "(6371 * acos(cos(radians(:lat)) * cos(radians(latitude)) " +
                "* cos(radians(longitude) - radians(:lng)) " +
                "+ sin(radians(:lat)) * sin(radians(latitude)))) AS distance " +
                "FROM vendors " +
                "WHERE vendor_type = :vendorType AND is_approved = 1 " +
                "HAVING distance < :radius",
        nativeQuery = true
    )
    List<Vendor> findNearbyVendors(
        @Param("vendorType") String vendorType,
        @Param("lat") double lat,
        @Param("lng") double lng,
        @Param("radius") double radiusKm
    );
    */
}
