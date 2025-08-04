package com.carocart.authentication.dto;

public class UserDTO {
    private Long id;
    private String email;
    private String role;
    private String firstName;
    private Double latitude;
    private Double longitude;

    public UserDTO() {}

    // ✅ Constructor used for internal/system use
    public UserDTO(Long id, String email, String role, String firstName) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.firstName = firstName;
    }

    // ✅ Full constructor including lat/lng
    public UserDTO(Long id, String email, String role, String firstName, Double latitude, Double longitude) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.firstName = firstName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
