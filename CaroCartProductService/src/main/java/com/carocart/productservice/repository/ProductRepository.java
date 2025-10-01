package com.carocart.productservice.repository;

import com.carocart.productservice.entity.Category;
import com.carocart.productservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // --- Paginated versions ---
    Page<Product> findBySubCategoryId(Long subCategoryId, Pageable pageable);

    Page<Product> findBySubCategory_Category_Id(Long categoryId, Pageable pageable);

    Page<Product> findByAddedByRoleAndVendorId(String role, Long vendorId, Pageable pageable);

    Page<Product> findByAddedByRoleAndVendorIdIn(String role, List<Long> vendorIds, Pageable pageable);

    Page<Product> findByAddedByRoleAndVendorIdInAndSubCategory_Id(String role, List<Long> vendorIds, Long subCategoryId, Pageable pageable);

    Page<Product> findByAddedByRoleAndVendorIdInAndSubCategory_Category_Id(String role, List<Long> vendorIds, Long categoryId, Pageable pageable);

    // --- Non-paginated / optional methods for backward compatibility ---
    List<Product> findBySubCategoryId(Long subCategoryId);

    List<Product> findBySubCategory_Category_Id(Long categoryId);

    Optional<Product> findBySlug(String slug);

    List<Product> findByAddedByRoleAndVendorId(String role, Long vendorId);

    List<Product> findByAddedByRoleAndVendorIdIn(String role, List<Long> vendorIds);

    List<Product> findByAddedByRoleAndVendorIdInAndSubCategory_Id(String role, List<Long> vendorIds, Long subCategoryId);

    List<Product> findByAddedByRoleAndVendorIdInAndSubCategory_Category_Id(String role, List<Long> vendorIds, Long categoryId);

    List<Product> findTop8ByNameIgnoreCaseContainingOrBrandIgnoreCaseContainingOrDescriptionIgnoreCaseContaining(
        String name, String brand, String description
    );

    // Vendor-specific categories
    @Query("SELECT DISTINCT p.subCategory.category FROM Product p WHERE p.vendorId = :vendorId")
    List<Category> findDistinctCategoriesByVendorId(@Param("vendorId") Long vendorId);

    @Query("SELECT DISTINCT p.vendorId FROM Product p WHERE p.subCategory.id = :subCategoryId")
    List<Long> findVendorIdsBySubCategoryId(@Param("subCategoryId") Long subCategoryId);

    long countByAddedByRoleAndVendorId(String addedByRole, Long vendorId);
}
