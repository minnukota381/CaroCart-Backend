package com.carocart.authentication.service;

import com.carocart.authentication.dto.AdminRegistrationDTO;
import com.carocart.authentication.entity.Admin;
import com.carocart.authentication.repository.AdminRepository;
import com.carocart.authentication.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public String signup(AdminRegistrationDTO dto) {
        if (adminRepository.findByEmail(dto.getEmail()).isPresent()) {
            return "Admin already exists";
        }
        Admin admin = new Admin();
        admin.setUsername(dto.getUsername());
        admin.setFirstName(dto.getFirstName());
        admin.setLastName(dto.getLastName());
        admin.setEmail(dto.getEmail());
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        admin.setRole("ADMIN");
        adminRepository.save(admin);
        return "Admin registered successfully";
    }

    @Override
    public String login(String email, String password) {
        Optional<Admin> optionalAdmin = adminRepository.findByEmail(email);
        if (optionalAdmin.isEmpty()) {
            return "Admin not found";
        }
        Admin admin = optionalAdmin.get();
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            return "Invalid credentials";
        }
        return jwtUtil.generateToken(
        		 admin.getId(), 
                admin.getEmail(),
                "ADMIN",
                admin.getFirstName(),
                admin.getLastName()
        );
    }

    @Override
    public Optional<Admin> findByEmail(String email) {
        return adminRepository.findByEmail(email);
    }

    @Override
    public Admin findAdminByEmail(String email) {
        return adminRepository.findByEmail(email).orElse(null);
    }

    @Override
    public void save(Admin admin) {
        adminRepository.save(admin);
    }

    @Override
    public boolean changePassword(String email, String currentPassword, String newPassword) {
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (passwordEncoder.matches(currentPassword, admin.getPassword())) {
                admin.setPassword(passwordEncoder.encode(newPassword));
                adminRepository.save(admin);
                return true;
            }
        }
        return false;
    }
}
