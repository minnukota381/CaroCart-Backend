package com.carocart.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SubCategoryDTO {
    @NotBlank(message = "Subcategory name is required")
    @Size(max = 50, message = "Subcategory name too long")
    private String name;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
