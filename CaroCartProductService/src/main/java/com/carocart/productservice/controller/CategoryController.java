package com.carocart.productservice.controller;

import com.carocart.productservice.dto.CategoryRequestDTO;
import com.carocart.productservice.dto.SubCategoryDTO;
import com.carocart.productservice.entity.Category;
import com.carocart.productservice.entity.SubCategory;
import com.carocart.productservice.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // ✅ Admin: Add category with validation
//    @PostMapping("/admin/add")
//    public ResponseEntity<Category> addCategory(@Valid @RequestBody CategoryRequestDTO dto,
//                                                @RequestHeader("Authorization") String token) {
//        // (Optional) Check for duplicate name in service if you want
//        Category category = new Category();
//        category.setName(dto.getName());
//        return ResponseEntity.ok(categoryService.addCategory(category, token));
//    }

    // ✅ Admin: Add subcategory with validation
    @PostMapping("/admin/subcategory/add")
    public ResponseEntity<SubCategory> addSubCategory(@Valid @RequestBody SubCategoryDTO dto,
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

    // ✅ User: Get all categories
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // ✅ User: Get subcategories by category
    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<SubCategory>> getSubCategories(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getSubCategoriesByCategoryId(id));
    }

    // ✅ Admin: Delete category
    @DeleteMapping("/admin/delete/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId,
                                                 @RequestHeader("Authorization") String token) {
        categoryService.deleteCategoryById(categoryId, token);
        return ResponseEntity.ok("Category deleted successfully");
    }

    // ✅ Admin: Delete subcategory
    @DeleteMapping("/admin/subcategory/delete/{subcategoryId}")
    public ResponseEntity<String> deleteSubCategory(@PathVariable Long subcategoryId,
                                                    @RequestHeader("Authorization") String token) {
        categoryService.deleteSubCategoryById(subcategoryId, token);
        return ResponseEntity.ok("Subcategory deleted successfully");
    }
    
    @PostMapping(value = "/admin/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Category> addCategory(
        @RequestPart("dto") String dtoJson,
        @RequestPart(value = "image", required = false) MultipartFile image,
        @RequestHeader("Authorization") String token
    ) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            CategoryRequestDTO dto = mapper.readValue(dtoJson, CategoryRequestDTO.class);

            Category category = new Category();
            category.setName(dto.getName());

            if (image != null && !image.isEmpty()) {
                category.setImage(image.getBytes());
            }

            // Leave imageUrl null for now
            Category saved = categoryService.addCategory(category, token);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping(value = "/admin/update/{categoryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long categoryId,
            @RequestPart("dto") String dtoJson,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestHeader("Authorization") String token
    ) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            CategoryRequestDTO dto = mapper.readValue(dtoJson, CategoryRequestDTO.class);

            Category existing = categoryService.getCategoryById(categoryId);
            if (existing == null) {
                return ResponseEntity.notFound().build();
            }
            // (Optional) Admin validation
            // update name if changed
            if (dto.getName() != null && !dto.getName().isBlank()) {
                existing.setName(dto.getName());
            }
            // update image if provided
            if (image != null && !image.isEmpty()) {
                existing.setImage(image.getBytes());
            }
            // Optionally: handle imageUrl here if you support S3/etc later
            Category saved = categoryService.saveCategory(existing, token);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    
//    @PostMapping("/admin/{id}/image-url")
//    public ResponseEntity<?> setCategoryImageUrl(
//            @PathVariable Long id,
//            @RequestParam("imageUrl") String imageUrl,
//            @RequestHeader("Authorization") String token) {
//        Category category = categoryService.getCategoryById(id);
//        if (category == null) return ResponseEntity.notFound().build();
//        category.setImageUrl(imageUrl);
//        categoryService.saveCategory(category);
//        return ResponseEntity.ok("Image URL set");
//    }
    
    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getCategoryImage(@PathVariable Long id) {
        Category cat = categoryService.getCategoryById(id);
        if (cat == null || cat.getImage() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG) // or detect type
            .body(cat.getImage());
    }
}
