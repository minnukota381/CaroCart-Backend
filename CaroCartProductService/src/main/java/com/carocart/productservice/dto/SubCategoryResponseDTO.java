// SubCategoryResponseDTO.java
package com.carocart.productservice.dto;

public class SubCategoryResponseDTO {
    private Long id;
    private String name;
    private Long categoryId;
    // + getters/setters
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
    
    
}
