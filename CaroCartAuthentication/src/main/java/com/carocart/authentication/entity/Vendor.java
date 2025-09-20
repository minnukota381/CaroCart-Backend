package com.carocart.authentication.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendors")
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Personal Info
    @NotBlank(message = "First name is mandatory")
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    private String lastName;

    @NotBlank(message = "Company name is mandatory")
    private String companyName;

    @Email(message = "Email should be valid")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Contact number is mandatory")
    private String contactNumber;

    // ✅ NEW: Optional alternate phone
    @Column(name = "alternate_phone", length = 20)
    private String alternatePhone;

    private String gstNumber;

    private boolean isApproved = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String manualCloseReason;
    private LocalDateTime manualCloseUntil;

    @Column(nullable = false)
    private String role = "VENDOR";

    // Vendor Type: FOOD, GROCERY, etc.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorType vendorType;

    // Address Info (normalized for search & display)
    @NotBlank(message = "Address Line 1 is required")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pin code is required")
    private String pinCode;

    // Geolocation for spatial queries (required for proximity search)
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    // Profile image URL (Firebase/public link)
    @Column(name = "profile_image_url", length = 2048)
    private String profileImageUrl;

    @Column(name = "upi_id", length = 50)
    private String upiId;

    // ✅ Morning timings (existing names; keep your current logic untouched)
    @Column(name = "opening_time", nullable = false)
    private String openingTime;

    @Column(name = "closing_time", nullable = false)
    private String closingTime;

    // ✅ NEW: Optional evening split-shift timings
    @Column(name = "evening_opening_time")
    private String eveningOpeningTime;  // e.g., "17:00" or null

    @Column(name = "evening_closing_time")
    private String eveningClosingTime;  // e.g., "22:00" or null

    // Availability Controls
    @Column(name = "accepting_orders", nullable = false)
    private Boolean acceptingOrders = Boolean.TRUE;
    
 // in Vendor.java (same file you shared)
    @Column(nullable = false, unique = true, length = 140)
    private String slug;

    @Column(name = "city_slug", length = 100)
    private String citySlug;

    // getters/setters for slug, citySlug
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getCitySlug() { return citySlug; }
    public void setCitySlug(String citySlug) { this.citySlug = citySlug; }


    // === Lifecycle hooks ===
    @PrePersist
    protected void onCreate() {
        if (acceptingOrders == null) acceptingOrders = Boolean.TRUE;
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        if (acceptingOrders == null) acceptingOrders = Boolean.TRUE;
        updatedAt = LocalDateTime.now();
    }

    // === Getters and Setters ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getAlternatePhone() { return alternatePhone; }
    public void setAlternatePhone(String alternatePhone) { this.alternatePhone = alternatePhone; }

    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }

    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean approved) { isApproved = approved; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public VendorType getVendorType() { return vendorType; }
    public void setVendorType(VendorType vendorType) { this.vendorType = vendorType; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPinCode() { return pinCode; }
    public void setPinCode(String pinCode) { this.pinCode = pinCode; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }

    public String getOpeningTime() { return openingTime; }
    public void setOpeningTime(String openingTime) { this.openingTime = openingTime; }

    public String getClosingTime() { return closingTime; }
    public void setClosingTime(String closingTime) { this.closingTime = closingTime; }

    public String getEveningOpeningTime() { return eveningOpeningTime; }
    public void setEveningOpeningTime(String eveningOpeningTime) { this.eveningOpeningTime = eveningOpeningTime; }

    public String getEveningClosingTime() { return eveningClosingTime; }
    public void setEveningClosingTime(String eveningClosingTime) { this.eveningClosingTime = eveningClosingTime; }

    public Boolean getAcceptingOrders() { return acceptingOrders; }
    public void setAcceptingOrders(Boolean acceptingOrders) { this.acceptingOrders = acceptingOrders; }

    public LocalDateTime getManualCloseUntil() { return manualCloseUntil; }
    public void setManualCloseUntil(LocalDateTime manualCloseUntil) { this.manualCloseUntil = manualCloseUntil; }

    public String getManualCloseReason() { return manualCloseReason; }
    public void setManualCloseReason(String manualCloseReason) { this.manualCloseReason = manualCloseReason; }
}
