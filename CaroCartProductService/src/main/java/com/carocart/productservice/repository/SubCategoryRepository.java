package com.carocart.productservice.repository;

import com.carocart.productservice.entity.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {

    List<SubCategory> findByCategoryId(Long categoryId);

    Optional<SubCategory> findBySlug(String slug);

    List<SubCategory> findTop5ByNameIgnoreCaseContaining(String name);

    // ✅ FIXED: Join via Product.subCategory
    @Query("SELECT DISTINCT p.subCategory FROM Product p " +
           "WHERE p.vendorId IN :vendorIds")
    List<SubCategory> findByVendorIds(@Param("vendorIds") List<Long> vendorIds);
}
