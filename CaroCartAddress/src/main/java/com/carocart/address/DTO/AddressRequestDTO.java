package com.carocart.address.DTO;

import jakarta.validation.constraints.*;

public class AddressRequestDTO {

    @NotBlank(message = "Full name is required")
    @Size(max = 50, message = "Full name must be at most 50 characters")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;

    @Pattern(regexp = "^$|^\\d{10}$", message = "Alternate phone must be 10 digits or empty")
    private String alternatePhone;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^\\d{6}$", message = "Pincode must be 6 digits")
    private String pincode;

    @NotBlank(message = "House/Flat Number is required")
    @Size(max = 30, message = "House/Flat Number must be at most 30 characters")
    private String houseNumber;

    @NotBlank(message = "Street is required")
    @Size(max = 60, message = "Street must be at most 60 characters")
    private String street;

    @Size(max = 60, message = "Landmark must be at most 60 characters")
    private String landmark;

    @NotBlank(message = "City is required")
    @Size(max = 30, message = "City must be at most 30 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 30, message = "State must be at most 30 characters")
    private String state;

    @NotBlank(message = "Country is required")
    @Size(max = 30, message = "Country must be at most 30 characters")
    private String country;

    @NotBlank(message = "Address type is required")
    @Pattern(regexp = "Home|Work|Other", message = "Address type must be Home, Work, or Other")
    private String addressType;

    private Boolean isDefault = false;

    // Getters and setters (same as before)
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAlternatePhone() { return alternatePhone; }
    public void setAlternatePhone(String alternatePhone) { this.alternatePhone = alternatePhone; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getHouseNumber() { return houseNumber; }
    public void setHouseNumber(String houseNumber) { this.houseNumber = houseNumber; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getAddressType() { return addressType; }
    public void setAddressType(String addressType) { this.addressType = addressType; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
}
