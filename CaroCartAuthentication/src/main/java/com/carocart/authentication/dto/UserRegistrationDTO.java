package com.carocart.authentication.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class UserRegistrationDTO {

    @NotBlank(message = "First name is required")
    @Size(max = 30, message = "First name must be at most 30 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 30, message = "Last name must be at most 30 characters")
    private String lastName;

    // Email is OPTIONAL; validate only if present
    @Email(message = "Enter a valid email")
    private String email;

    // Password is OPTIONAL; validate only if present
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    // Phone is REQUIRED (primary identity)
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;

    @Past(message = "DOB must be in the past")
    private LocalDate dob; // optional

    // Getters / Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
}
