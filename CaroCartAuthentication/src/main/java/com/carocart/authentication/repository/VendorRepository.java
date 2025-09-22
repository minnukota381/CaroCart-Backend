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

    // ===== Identity & duplicates =====
    Optional<Vendor> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByContactNumber(String contactNumber);
    boolean existsByGstNumber(String gstNumber);
    boolean existsByCompanyName(String companyName);

    // ===== Approval filters =====
    List<Vendor> findByIsApprovedFalse();
    List<Vendor> findByIsApprovedTrue();
    List<Vendor> findByVendorTypeAndIsApproved(VendorType vendorType, boolean isApproved);

    // ===== Slugs =====
    Optional<Vendor> findBySlug(String slug);
    boolean existsBySlug(String slug);

    // When updating an existing vendor, check if another row already has the slug
    boolean existsBySlugAndIdNot(String slug, Long id);

    // City-scoped pretty URL resolvers
    Optional<Vendor> findByCitySlugAndSlug(String citySlug, String slug);
    boolean existsByCitySlugAndSlug(String citySlug, String slug);

    // Helpers that ensure vendor is approved
    Optional<Vendor> findBySlugAndIsApprovedTrue(String slug);
    Optional<Vendor> findByCitySlugAndSlugAndIsApprovedTrue(String citySlug, String slug);

    // City-scoped lists (approved only)
    List<Vendor> findByCitySlugAndIsApprovedTrue(String citySlug, Pageable pageable);

    // ===== Search (includes slug & citySlug) =====
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
