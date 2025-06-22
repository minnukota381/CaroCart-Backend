package com.carocart.authentication.dto;

public class UserDTO {
    private Long id;
    private String email;
    private String role;
    private String firstName;  // ✅ Add this field

    public UserDTO() {}

    // ✅ Updated constructor
    public UserDTO(Long id, String email, String role, String firstName) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.firstName = firstName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
}
