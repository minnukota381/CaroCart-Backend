package com.carocart.authentication.dto;

import jakarta.validation.constraints.*;

public class VendorRegistrationDTO {

    @NotBlank(message = "First name is required")
    @Size(max = 30)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 30)
    private String lastName;

    @NotBlank(message = "Company name is required")
    @Size(max = 50)
    private String companyName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8)
    private String password;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Contact number must be 10 digits")
    private String contactNumber;

    // Optional, but if provided must match GSTIN format
    @Pattern(regexp = "^$|^[0-9A-Z]{15}$", message = "GST number must be 15 characters or empty")
    private String gstNumber;

    @NotBlank(message = "Business type is required")
    private String businessType;

    @NotBlank(message = "Business address is required")
    private String businessAddress;

    // Getters and setters
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

    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public String getBusinessAddress() { return businessAddress; }
    public void setBusinessAddress(String businessAddress) { this.businessAddress = businessAddress; }
}
