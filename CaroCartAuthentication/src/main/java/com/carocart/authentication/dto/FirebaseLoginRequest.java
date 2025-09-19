package com.carocart.authentication.dto;


public class FirebaseLoginRequest {
    private String idToken;   // Firebase ID token from the frontend
    private String phone;     // optional fallback
    private String firstName; // optional
    private String lastName;  // optional
    private String email;     // optional

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
