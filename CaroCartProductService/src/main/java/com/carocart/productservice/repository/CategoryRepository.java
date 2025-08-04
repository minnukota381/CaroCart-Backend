package com.carocart.productservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carocart.productservice.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameIgnoreCase(String name);
    Optional<Category> findBySlug(String slug);
    
    List<Category> findTop5ByNameIgnoreCaseContaining(String name);


}
