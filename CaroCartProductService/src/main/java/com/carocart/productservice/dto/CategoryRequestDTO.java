package com.carocart.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class CategoryRequestDTO {
    @NotBlank(message = "Category name is required")
    @Size(max = 50, message = "Category name too long")
    private String name;

    // Optional: Cloud image URL (used in prod)
    private String imageUrl;

    // Optional: For file upload (dev/BLOB)
    // Use @RequestPart in controller to map
    private MultipartFile imageFile;

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public MultipartFile getImageFile() { return imageFile; }
    public void setImageFile(MultipartFile imageFile) { this.imageFile = imageFile; }
}
