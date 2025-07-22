package com.carocart.home.entity;

import jakarta.persistence.*;
@Entity
public class HomeSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;         // "banner", "products", "shopByCategory", etc.
    private String bannerId;     // for banners
    private Long categoryId;     // for products
    private String sectionTitle; // display title
    private Integer sectionOrder;
    private Integer productLimit;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getBannerId() {
		return bannerId;
	}
	public void setBannerId(String bannerId) {
		this.bannerId = bannerId;
	}
	public Long getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
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
	public Integer getProductLimit() {
		return productLimit;
	}
	public void setProductLimit(Integer productLimit) {
		this.productLimit = productLimit;
	}

    // getters/setters, no Lombok
}
