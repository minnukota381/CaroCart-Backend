package com.carocart.authentication.repository;

import com.carocart.authentication.entity.Vendor;
import com.carocart.authentication.entity.VendorType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {

    // ===== Existing =====
    Optional<Vendor> findByEmail(String email);
    boolean existsByEmail(String email);

    List<Vendor> findByIsApprovedFalse();
    List<Vendor> findByIsApprovedTrue();
    List<Vendor> findByVendorTypeAndIsApproved(VendorType vendorType, boolean isApproved);

    // ===== New: Slug support =====
    Optional<Vendor> findBySlug(String slug);
    boolean existsBySlug(String slug);

    // Resolve a vendor uniquely by citySlug + vendorSlug (nice for pretty URLs)
    Optional<Vendor> findByCitySlugAndSlug(String citySlug, String slug);

    // Helpful resolvers that also ensure vendor is approved
    Optional<Vendor> findBySlugAndIsApprovedTrue(String slug);
    Optional<Vendor> findByCitySlugAndSlugAndIsApprovedTrue(String citySlug, String slug);

    // Optional: fetch approved vendors scoped to a city (use Pageable for limits)
    List<Vendor> findByCitySlugAndIsApprovedTrue(String citySlug, Pageable pageable);

    // ===== Updated search to include slug & citySlug =====
    @Query("""
        SELECT v FROM Vendor v
        WHERE v.isApproved = true
          AND (
              LOWER(v.companyName) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(v.city) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(COALESCE(v.addressLine1, '')) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(COALESCE(v.addressLine2, '')) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(v.slug) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(COALESCE(v.citySlug, '')) LIKE LOWER(CONCAT('%', :q, '%'))
          )
        """)
    List<Vendor> searchVendorsAnyType(@Param("q") String q, Pageable pageable);
}
