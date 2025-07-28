package com.carocart.home.dto;

import java.util.List;

public class HomeSectionResponseDTO {
    private String type;
    private String sectionTitle;
    private Integer sectionOrder;
    private BannerDTO banner;
    private List<ProductDTO> products;
    private List<CategoryDTO> categories;
    
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getSectionTitle() {
		return sectionTitle;
	}
	public void setSectionTitle(String sectionTitle) {
		this.sectionTitle = sectionTitle;
	}
	public Integer getSectionOrder() {
		return sectionOrder;
	}
	public void setSectionOrder(Integer sectionOrder) {
		this.sectionOrder = sectionOrder;
	}
	public BannerDTO getBanner() {
		return banner;
	}
	public void setBanner(BannerDTO banner) {
		this.banner = banner;
	}
	public List<ProductDTO> getProducts() {
		return products;
	}
	public void setProducts(List<ProductDTO> products) {
		this.products = products;
	}
	public List<CategoryDTO> getCategories() {
		return categories;
	}
	public void setCategories(List<CategoryDTO> categories) {
		this.categories = categories;
	}

    // Getters and Setters
    
}
