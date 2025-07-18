package com.carocart.productservice.controller;

import com.carocart.productservice.dto.CategoryRequestDTO;
import com.carocart.productservice.dto.CategoryResponseDTO;
import com.carocart.productservice.dto.SubCategoryDTO;
import com.carocart.productservice.dto.SubCategoryResponseDTO;
import com.carocart.productservice.entity.Category;
import com.carocart.productservice.entity.SubCategory;
import com.carocart.productservice.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // Add Category (Admin, with imageUrl)
    @PostMapping("/admin/add")
    public ResponseEntity<CategoryResponseDTO> addCategory(
            @Valid @RequestBody CategoryRequestDTO dto,
            @RequestHeader("Authorization") String token
    ) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setImageUrl(dto.getImageUrl()); // Firebase URL from frontend
        Category saved = categoryService.addCategory(category, token);
        return ResponseEntity.ok(toCategoryResponseDTO(saved));
    }

    // Update Category (Admin, with imageUrl)
    @PutMapping("/admin/update/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
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
        return ResponseEntity.ok(toCategoryResponseDTO(saved));
    }

    // Add Subcategory (Admin)
    @PostMapping("/admin/subcategory/add")
    public ResponseEntity<SubCategoryResponseDTO> addSubCategory(
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
        return ResponseEntity.ok(toSubCategoryResponseDTO(savedSubCategory));
    }

    // Get all categories (User/Admin)
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryResponseDTO> dtos = categories.stream()
                .map(this::toCategoryResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Get subcategories by category (User/Admin)
    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<SubCategoryResponseDTO>> getSubCategories(@PathVariable Long id) {
        List<SubCategory> subCategories = categoryService.getSubCategoriesByCategoryId(id);
        List<SubCategoryResponseDTO> dtos = subCategories.stream()
                .map(this::toSubCategoryResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
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

    // ===== Helper mapping methods =====

    private CategoryResponseDTO toCategoryResponseDTO(Category cat) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(cat.getId());
        dto.setName(cat.getName());
        dto.setImageUrl(cat.getImageUrl());
        return dto;
    }

    private SubCategoryResponseDTO toSubCategoryResponseDTO(SubCategory subCat) {
        SubCategoryResponseDTO dto = new SubCategoryResponseDTO();
        dto.setId(subCat.getId());
        dto.setName(subCat.getName());
        dto.setCategoryId(subCat.getCategory() != null ? subCat.getCategory().getId() : null);
        return dto;
    }
}
