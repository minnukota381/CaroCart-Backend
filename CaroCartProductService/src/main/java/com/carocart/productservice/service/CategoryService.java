package com.carocart.productservice.service;

import com.carocart.productservice.dto.SubCategoryDTO;
import com.carocart.productservice.dto.VendorResponseDTO;
import com.carocart.productservice.entity.Category;
import com.carocart.productservice.entity.SubCategory;

import java.util.List;

public interface CategoryService {
    Category addCategory(Category category, String token);
    SubCategory addSubCategory(SubCategory subCategory, String token);
    List<Category> getAllCategories();
    List<SubCategory> getSubCategoriesByCategoryId(Long categoryId);
    SubCategory getSubCategoryById(Long id);

    Category getCategoryById(Long id);
    void deleteCategoryById(Long categoryId, String token);
    void deleteSubCategoryById(Long subcategoryId, String token);
    Category saveCategory(Category category);
    Category saveCategory(Category category, String token);

    Category getCategoryBySlug(String slug);
    SubCategory getSubCategoryBySlug(String slug);
    void generateMissingSlugsForCategories();
    void generateMissingSlugsForSubCategories();

    List<Category> getCategoriesByVendorId(Long vendorId);
    List<SubCategory> getSubCategoriesByVendorIds(List<Long> vendorIds);

    List<VendorResponseDTO> getNearbyVendorsBySubCategory(Long subCategoryId, double lat, double lng, double radiusKm);
    List<Long> getVendorIdsBySubCategory(Long subCategoryId);

    // ✅ NEW: update subcategory (name/imageUrl/category)
    SubCategory updateSubCategory(Long id, SubCategoryDTO dto, String token);
    SubCategory saveSubCategory(SubCategory subCategory, String token);
    void validateAdmin(String token);

}
