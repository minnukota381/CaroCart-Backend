package com.carocart.authentication.dto;

import com.carocart.authentication.entity.VendorType;

public class VendorResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String companyName;
    private String contactNumber;
    private String gstNumber;

    // Vendor type
    private VendorType vendorType;

    // Address fields
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pinCode;

    // Geolocation
    private Double latitude;
    private Double longitude;

    // Profile image URL (Firebase/public link)
    private String profileImageUrl;

    // Approved status
    private Boolean approved;
    private String upiId;

    // Opening/closing (store as plain strings)
    private String openingTime;
    private String closingTime;

    // Extras
    private Integer etaMinutes;
    private Boolean acceptingOrders;

    // ✅ NEW: manual close info
    private String manualCloseReason;   // optional note shown in admin
    private String manualCloseUntil;    // ISO-8601 string, e.g. "2025-08-16T23:00:00"

    // ===== getters/setters =====

    public Boolean getAcceptingOrders() { return acceptingOrders; }
    public void setAcceptingOrders(Boolean acceptingOrders) { this.acceptingOrders = acceptingOrders; }

    public String getManualCloseReason() { return manualCloseReason; }
    public void setManualCloseReason(String manualCloseReason) { this.manualCloseReason = manualCloseReason; }

    public String getManualCloseUntil() { return manualCloseUntil; }
    public void setManualCloseUntil(String manualCloseUntil) { this.manualCloseUntil = manualCloseUntil; }

    public Integer getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(Integer etaMinutes) { this.etaMinutes = etaMinutes; }

    public String getOpeningTime() { return openingTime; }
    public void setOpeningTime(String openingTime) { this.openingTime = openingTime; }

    public String getClosingTime() { return closingTime; }
    public void setClosingTime(String closingTime) { this.closingTime = closingTime; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }

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

    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }
}
