package com.carocart.productservice.repository;

import com.carocart.productservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameIgnoreCase(String name);

    Optional<Category> findBySlug(String slug);

    List<Category> findTop5ByNameIgnoreCaseContaining(String name);

    // ✅ FIXED: Join via Product -> SubCategory -> Category
    @Query("SELECT DISTINCT sc.category FROM Product p " +
           "JOIN p.subCategory sc " +
           "WHERE p.vendorId = :vendorId")
    List<Category> findByVendorId(@Param("vendorId") Long vendorId);
}
