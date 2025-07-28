package com.carocart.home.dto;

public class BannerDTO {
    private String bannerId;
    private String imageUrl;
    private String linkTo;
    private String alt;
    private Integer sectionOrder;
	public String getBannerId() {
		return bannerId;
	}
	public void setBannerId(String bannerId) {
		this.bannerId = bannerId;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getLinkTo() {
		return linkTo;
	}
	public void setLinkTo(String linkTo) {
		this.linkTo = linkTo;
	}
	public String getAlt() {
		return alt;
	}
	public void setAlt(String alt) {
		this.alt = alt;
	}
	public Integer getSectionOrder() {
		return sectionOrder;
	}
	public void setSectionOrder(Integer sectionOrder) {
		this.sectionOrder = sectionOrder;
	}

    // Getters and Setters
    
}
