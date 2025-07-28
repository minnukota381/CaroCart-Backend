package com.carocart.authentication.dto;

public class DeliveryBoyDTO {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String profilePhotoUrl;
    private String aadhaarUrl;
    private String licenseUrl;
    private String panUrl;
    private String status;

    // Getters and setters (same as your delivery DTO)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    public String getAadhaarUrl() { return aadhaarUrl; }
    public void setAadhaarUrl(String aadhaarUrl) { this.aadhaarUrl = aadhaarUrl; }

    public String getLicenseUrl() { return licenseUrl; }
    public void setLicenseUrl(String licenseUrl) { this.licenseUrl = licenseUrl; }

    public String getPanUrl() { return panUrl; }
    public void setPanUrl(String panUrl) { this.panUrl = panUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
