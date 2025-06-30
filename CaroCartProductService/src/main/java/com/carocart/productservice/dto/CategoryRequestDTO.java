package com.carocart.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryRequestDTO {
    @NotBlank(message = "Category name is required")
    @Size(max = 50, message = "Category name too long")
    private String name;

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
