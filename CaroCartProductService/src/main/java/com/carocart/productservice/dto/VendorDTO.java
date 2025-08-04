package com.carocart.productservice.dto;

public class VendorDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role; // should be "VENDOR"
    private String vendorType; // "FOOD" or "GROCERY"
    private Boolean approved; // optional but useful for checks

    // Optional helper
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // --- Getters and setters ---
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

    public String getVendorType() { return vendorType; }
    public void setVendorType(String vendorType) { this.vendorType = vendorType; }

    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }
}
