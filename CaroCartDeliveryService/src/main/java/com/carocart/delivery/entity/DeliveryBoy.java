// DeliveryBoy.java (Entity)
package com.carocart.delivery.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
public class DeliveryBoy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Personal Info ---
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    @Column(nullable=false, unique=true)
    private String phone;
    @Column(unique=true)
    private String whatsapp;
    private String city;
    @Column(length=512)
    private String address;
    private String[] languages; // comma separated
    private String profilePhotoUrl;

    // --- Documents ---
    private String aadhaarFrontUrl;
    private String aadhaarBackUrl;
    private String panUrl;
    private String licenseUrl;
    private String rcUrl;
    private String passbookUrl;

    // --- Vehicle ---
    private String vehicleType;
    private String vehicleRegNo;
    private String drivingLicenseNumber;

    // --- Bank ---
    private String accountHolderName;
    private String accountNumber;
    private String bankName;
    private String ifscCode;
    private String upiId;

    // --- Auth & Status ---
    @Column(unique=true)
    private String email;
    @Column(nullable=false)
    private String passwordHash;
    private Boolean active = true;
    private String status = "PENDING";
    
    @Column(name = "current_lat")
    private Double currentLat;

    @Column(name = "current_lng")
    private Double currentLng;

    // ...getters and setters...
    public Double getCurrentLat() { return currentLat; }
    public void setCurrentLat(Double currentLat) { this.currentLat = currentLat; }
    public Double getCurrentLng() { return currentLng; }
    public void setCurrentLng(Double currentLng) { this.currentLng = currentLng; }

 // Add at the end of your DeliveryBoy entity:
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}
	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getWhatsapp() {
		return whatsapp;
	}
	public void setWhatsapp(String whatsapp) {
		this.whatsapp = whatsapp;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}


	public String[] getLanguages() {
		return languages;
	}
	public void setLanguages(String[] languages) {
		this.languages = languages;
	}
	public String getProfilePhotoUrl() {
		return profilePhotoUrl;
	}
	public void setProfilePhotoUrl(String profilePhotoUrl) {
		this.profilePhotoUrl = profilePhotoUrl;
	}
	public String getAadhaarFrontUrl() {
		return aadhaarFrontUrl;
	}
	public void setAadhaarFrontUrl(String aadhaarFrontUrl) {
		this.aadhaarFrontUrl = aadhaarFrontUrl;
	}
	public String getAadhaarBackUrl() {
		return aadhaarBackUrl;
	}
	public void setAadhaarBackUrl(String aadhaarBackUrl) {
		this.aadhaarBackUrl = aadhaarBackUrl;
	}
	public String getPanUrl() {
		return panUrl;
	}
	public void setPanUrl(String panUrl) {
		this.panUrl = panUrl;
	}
	public String getLicenseUrl() {
		return licenseUrl;
	}
	public void setLicenseUrl(String licenseUrl) {
		this.licenseUrl = licenseUrl;
	}
	public String getRcUrl() {
		return rcUrl;
	}
	public void setRcUrl(String rcUrl) {
		this.rcUrl = rcUrl;
	}
	public String getPassbookUrl() {
		return passbookUrl;
	}
	public void setPassbookUrl(String passbookUrl) {
		this.passbookUrl = passbookUrl;
	}
	public String getVehicleType() {
		return vehicleType;
	}
	public void setVehicleType(String vehicleType) {
		this.vehicleType = vehicleType;
	}
	public String getVehicleRegNo() {
		return vehicleRegNo;
	}
	public void setVehicleRegNo(String vehicleRegNo) {
		this.vehicleRegNo = vehicleRegNo;
	}
	public String getDrivingLicenseNumber() {
		return drivingLicenseNumber;
	}
	public void setDrivingLicenseNumber(String drivingLicenseNumber) {
		this.drivingLicenseNumber = drivingLicenseNumber;
	}
	public String getAccountHolderName() {
		return accountHolderName;
	}
	public void setAccountHolderName(String accountHolderName) {
		this.accountHolderName = accountHolderName;
	}
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getIfscCode() {
		return ifscCode;
	}
	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
	}
	public String getUpiId() {
		return upiId;
	}
	public void setUpiId(String upiId) {
		this.upiId = upiId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPasswordHash() {
		return passwordHash;
	}
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
	public Boolean getActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}


    // ...getters & setters...
}
