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

    // ✅ Admin: Add category with validation
    @PostMapping("/admin/add")
    public ResponseEntity<Category> addCategory(@Valid @RequestBody CategoryRequestDTO dto,
                                                @RequestHeader("Authorization") String token) {
        // (Optional) Check for duplicate name in service if you want
        Category category = new Category();
        category.setName(dto.getName());
        return ResponseEntity.ok(categoryService.addCategory(category, token));
    }

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
}
