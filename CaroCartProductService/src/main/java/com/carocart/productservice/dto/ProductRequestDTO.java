package com.carocart.productservice.dto;

import jakarta.validation.constraints.*;

public class ProductRequestDTO {
    // --- SHARED FIELDS ---
    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name too long")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description too long")
    private String description;

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
    private String imageUrl;
    // ProductType must be provided: GROCERY or FOOD
    @NotNull(message = "Product type is required")
    private String productType; // Could use enum or String, depends on how you handle in controller

    // --- GROCERY-SPECIFIC FIELDS ---
    private String brand;
    private String packSize;
    private String manufacturer;
    private String fssaiLicenseNo;
    private String countryOfOrigin;
    private String ingredients;
    private String nutritionInfo;
    private String returnPolicy;

    // --- FOOD-SPECIFIC FIELDS ---
    private String foodCategory; // e.g., Main Course, Starter
    private Boolean veg;
    private String restaurantName;
    private String customizationOptions; // JSON string
    private String addOns; // JSON string
    private Boolean bestseller;

    // --- Getters and Setters ---

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

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

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    // Grocery-specific
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getPackSize() { return packSize; }
    public void setPackSize(String packSize) { this.packSize = packSize; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getFssaiLicenseNo() { return fssaiLicenseNo; }
    public void setFssaiLicenseNo(String fssaiLicenseNo) { this.fssaiLicenseNo = fssaiLicenseNo; }

    public String getCountryOfOrigin() { return countryOfOrigin; }
    public void setCountryOfOrigin(String countryOfOrigin) { this.countryOfOrigin = countryOfOrigin; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getNutritionInfo() { return nutritionInfo; }
    public void setNutritionInfo(String nutritionInfo) { this.nutritionInfo = nutritionInfo; }

    public String getReturnPolicy() { return returnPolicy; }
    public void setReturnPolicy(String returnPolicy) { this.returnPolicy = returnPolicy; }

    // Food-specific
    public String getFoodCategory() { return foodCategory; }
    public void setFoodCategory(String foodCategory) { this.foodCategory = foodCategory; }

    public Boolean getVeg() { return veg; }
    public void setVeg(Boolean veg) { this.veg = veg; }

    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }

    public String getCustomizationOptions() { return customizationOptions; }
    public void setCustomizationOptions(String customizationOptions) { this.customizationOptions = customizationOptions; }

    public String getAddOns() { return addOns; }
    public void setAddOns(String addOns) { this.addOns = addOns; }

    public Boolean getBestseller() { return bestseller; }
    public void setBestseller(Boolean bestseller) { this.bestseller = bestseller; }
}
