package com.carocart.productservice.controller;

import com.carocart.productservice.dto.CategoryRequestDTO;
import com.carocart.productservice.dto.CategoryResponseDTO;
import com.carocart.productservice.dto.SubCategoryDTO;
import com.carocart.productservice.dto.SubCategoryResponseDTO;
import com.carocart.productservice.dto.VendorResponseDTO;
import com.carocart.productservice.entity.Category;
import com.carocart.productservice.entity.SubCategory;
import com.carocart.productservice.repository.SubCategoryRepository;
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
    
    @Autowired
    private SubCategoryRepository subCategoryRepository;

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
        dto.setSlug(cat.getSlug()); // ✅ Add this line
        return dto;
    }


    private SubCategoryResponseDTO toSubCategoryResponseDTO(SubCategory subCat) {
        SubCategoryResponseDTO dto = new SubCategoryResponseDTO();
        dto.setId(subCat.getId());
        dto.setName(subCat.getName());
        dto.setSlug(subCat.getSlug());
        
        if (subCat.getCategory() != null) {
            dto.setCategoryId(subCat.getCategory().getId());
            dto.setCategoryName(subCat.getCategory().getName());     // ✅
            dto.setCategorySlug(subCat.getCategory().getSlug());     // ✅
        }

        return dto;
    }

    
 // ⚠️ TEMPORARY: Generate missing slugs for categories
    @GetMapping("/admin/generate-category-slugs")
    public ResponseEntity<String> generateCategorySlugs(@RequestHeader("Authorization") String token) {
        categoryService.generateMissingSlugsForCategories();
        return ResponseEntity.ok("Category slugs generated (if missing).");
    }

    // ⚠️ TEMPORARY: Generate missing slugs for subcategories
    @GetMapping("/admin/generate-subcategory-slugs")
    public ResponseEntity<String> generateSubCategorySlugs(@RequestHeader("Authorization") String token) {
        categoryService.generateMissingSlugsForSubCategories();
        return ResponseEntity.ok("Subcategory slugs generated (if missing).");
    }
    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getCategoryBySlug(@PathVariable String slug) {
        if (slug.matches("\\d+")) {
            // Handle legacy numeric slugs by redirecting
            Long id = Long.parseLong(slug);
            Category cat = categoryService.getCategoryById(id);
            if (cat != null && cat.getSlug() != null) {
                return ResponseEntity.status(301)
                    .header("Location", "/categories/slug/" + cat.getSlug())
                    .build();
            }
            return ResponseEntity.notFound().build();
        }

        Category category = categoryService.getCategoryBySlug(slug);
        return category != null ? ResponseEntity.ok(category) : ResponseEntity.notFound().build();
    }

    @GetMapping("/subcategory/slug/{slug}")
    public ResponseEntity<?> getSubCategoryBySlug(@PathVariable String slug) {
        if (slug.matches("\\d+")) {
            Long id = Long.parseLong(slug);
            SubCategory sub = categoryService.getSubCategoryById(id);
            if (sub != null && sub.getSlug() != null) {
                return ResponseEntity.status(301)
                    .header("Location", "/categories/subcategory/slug/" + sub.getSlug())
                    .build();
            }
            return ResponseEntity.notFound().build();
        }

        SubCategory sub = categoryService.getSubCategoryBySlug(slug);
        return sub != null ? ResponseEntity.ok(sub) : ResponseEntity.notFound().build();
    }
    
 // Get all categories served by a specific vendor
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<CategoryResponseDTO>> getCategoriesByVendor(@PathVariable Long vendorId) {
        List<Category> categories = categoryService.getCategoriesByVendorId(vendorId);
        List<CategoryResponseDTO> dtos = categories.stream()
            .map(this::toCategoryResponseDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }// At the bottom of your CategoryController



	@GetMapping("/subcategories/all")
	public ResponseEntity<List<SubCategoryResponseDTO>> getAllSubCategories() {
	    List<SubCategory> subCategories = subCategoryRepository.findAll();
	    List<SubCategoryResponseDTO> dtos = subCategories.stream()
	        .map(this::toSubCategoryResponseDTO)
	        .collect(Collectors.toList());
	    return ResponseEntity.ok(dtos);
	}
	
	    
	@GetMapping("/vendor-subcategories")
	public ResponseEntity<List<SubCategoryResponseDTO>> getSubCategoriesByVendorIds(
	        @RequestParam List<Long> vendorIds) {
	    List<SubCategory> subCats = categoryService.getSubCategoriesByVendorIds(vendorIds);
	    List<SubCategoryResponseDTO> dtos = subCats.stream()
	            .map(this::toSubCategoryResponseDTO)
	            .toList();
	    return ResponseEntity.ok(dtos);
	}
	
	@GetMapping("/subcategory/{subCategoryId}/vendors/nearby")
	public ResponseEntity<List<VendorResponseDTO>> getNearbyVendorsBySubCategory(
	        @PathVariable Long subCategoryId,
	        @RequestParam double lat,
	        @RequestParam double lng,
	        @RequestParam(defaultValue = "10") double radiusKm) {

	    List<VendorResponseDTO> vendors =
	            categoryService.getNearbyVendorsBySubCategory(subCategoryId, lat, lng, radiusKm);

	    return ResponseEntity.ok(vendors);
	}

}
