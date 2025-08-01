// src/main/java/com/carocart/productservice/dto/SearchResultDTO.java
package com.carocart.productservice.dto;

import java.util.List;

public class SearchResultDTO {
    private List<ProductDTO> products;
    private List<CategoryResponseDTO> categories;
    private List<String> brands;
    
    private List<SubCategoryResponseDTO> subCategories;

	 // Update getter & setter:
	 public List<SubCategoryResponseDTO> getSubCategories() {
	     return subCategories;
	 }
	 public void setSubCategories(List<SubCategoryResponseDTO> subCategories) {
	     this.subCategories = subCategories;
	 }

    // Getters & Setters
    public List<ProductDTO> getProducts() { return products; }
    public void setProducts(List<ProductDTO> products) { this.products = products; }
    public List<CategoryResponseDTO> getCategories() { return categories; }
    public void setCategories(List<CategoryResponseDTO> categories) { this.categories = categories; }
    public List<String> getBrands() { return brands; }
    public void setBrands(List<String> brands) { this.brands = brands; }
}
