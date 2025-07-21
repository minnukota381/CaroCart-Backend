package com.carocart.productservice.service;

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


}
