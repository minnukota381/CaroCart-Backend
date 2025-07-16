package com.carocart.productservice.controller;

import com.carocart.productservice.dto.CategoryRequestDTO;
import com.carocart.productservice.dto.SubCategoryDTO;
import com.carocart.productservice.entity.Category;
import com.carocart.productservice.entity.SubCategory;
import com.carocart.productservice.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // Add Category (Admin, with imageUrl)
    @PostMapping("/admin/add")
    public ResponseEntity<Category> addCategory(
            @Valid @RequestBody CategoryRequestDTO dto,
            @RequestHeader("Authorization") String token
    ) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setImageUrl(dto.getImageUrl()); // Use Firebase/S3 URL from frontend
        Category saved = categoryService.addCategory(category, token);
        return ResponseEntity.ok(saved);
    }

    // Update Category (Admin, with imageUrl)
    @PutMapping("/admin/update/{categoryId}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequestDTO dto,
            @RequestHeader("Authorization") String token
    ) {
        Category existing = categoryService.getCategoryById(categoryId);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        if (dto.getName() != null && !dto.getName().isBlank()) {
            existing.setName(dto.getName());
        }
        if (dto.getImageUrl() != null && !dto.getImageUrl().isBlank()) {
            existing.setImageUrl(dto.getImageUrl());
        }
        Category saved = categoryService.saveCategory(existing, token);
        return ResponseEntity.ok(saved);
    }

    // Add Subcategory (Admin)
    @PostMapping("/admin/subcategory/add")
    public ResponseEntity<SubCategory> addSubCategory(
            @Valid @RequestBody SubCategoryDTO dto,
            @RequestHeader("Authorization") String token) {
        SubCategory subCategory = new SubCategory();
        subCategory.setName(dto.getName());
        Category category = categoryService.getCategoryById(dto.getCategoryId());
        if (category == null) {
            return ResponseEntity.badRequest().build();
        }
        subCategory.setCategory(category);
        SubCategory savedSubCategory = categoryService.addSubCategory(subCategory, token);
        return ResponseEntity.ok(savedSubCategory);
    }

    // Get all categories (User/Admin)
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // Get subcategories by category (User/Admin)
    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<SubCategory>> getSubCategories(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getSubCategoriesByCategoryId(id));
    }

    // Delete Category (Admin)
    @DeleteMapping("/admin/delete/{categoryId}")
    public ResponseEntity<String> deleteCategory(
            @PathVariable Long categoryId,
            @RequestHeader("Authorization") String token) {
        categoryService.deleteCategoryById(categoryId, token);
        return ResponseEntity.ok("Category deleted successfully");
    }

    // Delete Subcategory (Admin)
    @DeleteMapping("/admin/subcategory/delete/{subcategoryId}")
    public ResponseEntity<String> deleteSubCategory(
            @PathVariable Long subcategoryId,
            @RequestHeader("Authorization") String token) {
        categoryService.deleteSubCategoryById(subcategoryId, token);
        return ResponseEntity.ok("Subcategory deleted successfully");
    }

}
