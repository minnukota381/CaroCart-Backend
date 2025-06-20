// package: com.carocart.orderservice.dto

package com.carocart.orderservice.dto;

public class AddressDTO {
    private Long id;
    private String fullName;
    private String houseNumber;
    private String street;
    private String landmark;
    private String city;
    private String state;
    private String pincode;
    private String phoneNumber;
    private String addressType;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public String toFormattedString() {
        return fullName + ", " +
               houseNumber + ", " +
               street + ", " +
               (landmark != null ? landmark + ", " : "") +
               city + ", " +
               state + " - " +
               pincode + ". Phone: " + phoneNumber + " [" + addressType + "]";
    }
}
