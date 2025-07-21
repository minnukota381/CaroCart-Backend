package com.carocart.productservice.dto;

public class SubCategoryResponseDTO {
    private Long id;
    private String name;
    private Long categoryId;
    private String slug;
    private String categoryName;   // ✅ NEW
    private String categorySlug;   // ✅ NEW

    // Getters & Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Long getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getSlug() {
        return slug;
    }
    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getCategoryName() {
        return categoryName;
    }
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategorySlug() {
        return categorySlug;
    }
    public void setCategorySlug(String categorySlug) {
        this.categorySlug = categorySlug;
    }
}
