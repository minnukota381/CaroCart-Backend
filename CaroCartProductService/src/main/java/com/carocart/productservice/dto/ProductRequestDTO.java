package com.carocart.productservice.dto;

import jakarta.validation.constraints.*;

public class ProductRequestDTO {
    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name too long")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description too long")
    private String description;

    @NotBlank(message = "Brand is required")
    @Size(max = 60, message = "Brand too long")
    private String brand;

    @Positive(message = "Price must be greater than zero")
    private double price;

    @Positive(message = "MRP must be greater than zero")
    private double mrp;

    @Min(value = 0, message = "Discount cannot be negative")
    @Max(value = 100, message = "Discount cannot be more than 100%")
    private double discount;

    @Min(value = 0, message = "Stock can't be negative")
    private int stock;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotNull(message = "Subcategory ID is required")
    private Long subCategoryId;

    private Boolean isAvailable = true;

    // Image will be handled in the controller as MultipartFile, not here

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getMrp() { return mrp; }
    public void setMrp(double mrp) { this.mrp = mrp; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Long getSubCategoryId() { return subCategoryId; }
    public void setSubCategoryId(Long subCategoryId) { this.subCategoryId = subCategoryId; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
}
