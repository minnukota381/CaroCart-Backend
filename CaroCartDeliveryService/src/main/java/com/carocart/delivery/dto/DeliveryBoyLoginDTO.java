package com.carocart.delivery.dto;

public class DeliveryBoyLoginDTO {
    private String phoneOrEmail;
    private String password;

    public DeliveryBoyLoginDTO() {}

    public DeliveryBoyLoginDTO(String phoneOrEmail, String password) {
        this.phoneOrEmail = phoneOrEmail;
        this.password = password;
    }

    // Getters and setters
    public String getPhoneOrEmail() { return phoneOrEmail; }
    public void setPhoneOrEmail(String phoneOrEmail) { this.phoneOrEmail = phoneOrEmail; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
