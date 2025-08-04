package com.carocart.delivery.dto;


public class DeliveryBoyRegistrationDTO {

    // Personal Information
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String pincode;
    private String city;
    private String state;

    private String whatsapp;
    private String profilePhotoUrl;
    private String dateOfBirth;
    private String drivingLicenseNumber;
    private String passbookUrl;
    private String upiId;

    // Document URLs (Firebase)
    private String aadhaarFrontUrl;
    private String aadhaarBackUrl;
    private String panUrl; // instead of panCardUrl

    // Vehicle Information
    private String vehicleType;
    private String vehicleNumber;
    private String licenseUrl;
    private String rcBookUrl;

    // Bank Information
    private String accountHolderName;
    private String accountNumber;
    private String ifscCode;
    private String bankName;
    private String branchName;

    // Login Info
    private String password;

    // Optional
    private String[] languages;

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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
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

	public String getVehicleType() {
		return vehicleType;
	}

	public void setVehicleType(String vehicleType) {
		this.vehicleType = vehicleType;
	}

	public String getVehicleNumber() {
		return vehicleNumber;
	}

	public void setVehicleNumber(String vehicleNumber) {
		this.vehicleNumber = vehicleNumber;
	}

	public String getLicenseUrl() {
		return licenseUrl;
	}

	public void setLicenseUrl(String licenseUrl) {
		this.licenseUrl = licenseUrl;
	}

	public String getRcBookUrl() {
		return rcBookUrl;
	}

	public void setRcBookUrl(String rcBookUrl) {
		this.rcBookUrl = rcBookUrl;
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

	public String getIfscCode() {
		return ifscCode;
	}

	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String[] getLanguages() {
		return languages;
	}

	public void setLanguages(String[] languages) {
		this.languages = languages;
	}

	public String getWhatsapp() {
		return whatsapp;
	}

	public void setWhatsapp(String whatsapp) {
		this.whatsapp = whatsapp;
	}

	public String getProfilePhotoUrl() {
		return profilePhotoUrl;
	}

	public void setProfilePhotoUrl(String profilePhotoUrl) {
		this.profilePhotoUrl = profilePhotoUrl;
	}

	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getDrivingLicenseNumber() {
		return drivingLicenseNumber;
	}

	public void setDrivingLicenseNumber(String drivingLicenseNumber) {
		this.drivingLicenseNumber = drivingLicenseNumber;
	}

	public String getPassbookUrl() {
		return passbookUrl;
	}

	public void setPassbookUrl(String passbookUrl) {
		this.passbookUrl = passbookUrl;
	}

	public String getUpiId() {
		return upiId;
	}

	public void setUpiId(String upiId) {
		this.upiId = upiId;
	}
    
    
}
