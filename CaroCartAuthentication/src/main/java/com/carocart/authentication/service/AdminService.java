package com.carocart.authentication.service;

import com.carocart.authentication.dto.AdminRegistrationDTO;
import com.carocart.authentication.entity.Admin;

import java.util.Optional;

public interface AdminService {
    String signup(AdminRegistrationDTO dto);
    String login(String email, String password);
    Optional<Admin> findByEmail(String email);
    Admin findAdminByEmail(String email);
    void save(Admin admin);
    boolean changePassword(String email, String currentPassword, String newPassword);

    // Add this method:
    boolean updateProfileImageUrl(String email, String imageUrl);
}
