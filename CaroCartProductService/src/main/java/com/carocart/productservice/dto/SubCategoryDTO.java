package com.carocart.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class SubCategoryDTO {
    @NotBlank(message = "Subcategory name is required")
    @Size(max = 50, message = "Subcategory name too long")
    private String name;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    // Optional: Cloud image URL
    private String imageUrl;

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
