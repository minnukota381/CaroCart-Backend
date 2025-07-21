package com.carocart.productservice.repository;

import com.carocart.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findBySubCategoryId(Long subCategoryId);
    List<Product> findByAddedByRoleAndAddedById(String role, Long vendorId);
	List<Product> findBySubCategory_Category_Id(Long categoryId);
	Optional<Product> findBySlug(String slug);

}
