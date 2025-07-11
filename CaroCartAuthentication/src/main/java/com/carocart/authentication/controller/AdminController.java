package com.carocart.authentication.controller;

import com.carocart.authentication.dto.AdminRegistrationDTO;
import com.carocart.authentication.dto.AdminResponseDTO;
import com.carocart.authentication.entity.Admin;
import com.carocart.authentication.repository.AdminRepository;
import com.carocart.authentication.service.AdminService;
import com.carocart.authentication.util.JwtUtil;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admins")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // -- ADMIN REGISTRATION (DTO + Validation) --
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody AdminRegistrationDTO dto) {
        String result = adminService.signup(dto);
        if ("Admin registered successfully".equals(result)) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    // -- ADMIN LOGIN --
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        String token = adminService.login(email, password);

        if (token.equals("Admin not found") || token.equals("Invalid credentials")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", token));
        }
        return ResponseEntity.ok(Map.of("token", token, "role", "ADMIN"));
    }

    // -- GET CURRENT ADMIN (JWT based) --
    @GetMapping("/me")
    public ResponseEntity<AdminResponseDTO> getCurrentAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = auth.getName();
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        AdminResponseDTO dto = new AdminResponseDTO(
                admin.getId(),
                admin.getUsername(),
                admin.getEmail(),
                admin.getRole()
        );
        return ResponseEntity.ok(dto);
    }

    // -- GET PROFILE --
    @GetMapping("/profile")
    public ResponseEntity<Admin> getAdminProfile(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);
        Optional<Admin> adminOpt = adminService.findByEmail(email);
        return adminOpt.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // -- UPDATE PROFILE --
    @PutMapping("/profile")
    public ResponseEntity<String> updateAdminProfile(@RequestHeader("Authorization") String authHeader,
                                                     @RequestBody Admin updatedAdmin) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
        }
        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);
        Admin existingAdmin = adminService.findAdminByEmail(email);
        if (existingAdmin == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found");
        }
        existingAdmin.setFirstName(updatedAdmin.getFirstName());
        existingAdmin.setLastName(updatedAdmin.getLastName());
        existingAdmin.setUsername(updatedAdmin.getUsername());
        adminService.save(existingAdmin);
        return ResponseEntity.ok("Admin profile updated successfully");
    }

    // -- UPLOAD PROFILE IMAGE --
    @PostMapping("/profile/upload-image")
    public ResponseEntity<String> uploadProfileImage(
            @RequestParam("profileImage") MultipartFile file,
            @RequestHeader("Authorization") String authHeader) {
        long maxSize = 2 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            return ResponseEntity.badRequest().body("File size exceeds 2MB limit");
        }
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);
            Optional<Admin> adminOpt = adminRepository.findByEmail(email);
            if (adminOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found");
            }
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file to upload");
            }
            String contentType = file.getContentType();
            if (contentType == null ||
                !(contentType.equals("image/jpeg") ||
                  contentType.equals("image/png") ||
                  contentType.equals("image/gif"))) {
                return ResponseEntity.badRequest().body("Only JPEG, PNG, or GIF images are allowed");
            }
            Admin admin = adminOpt.get();
            admin.setProfileImage(file.getBytes());
            adminRepository.save(admin);
            return ResponseEntity.ok("Profile image updated successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process image: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }

    // -- GET PROFILE IMAGE --
    @GetMapping("/profile/image")
    public ResponseEntity<byte[]> getProfileImage(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isEmpty() || adminOpt.get().getProfileImage() == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] image = adminOpt.get().getProfileImage();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(image, headers, HttpStatus.OK);
    }

    // -- CHANGE PASSWORD --
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");
        boolean success = adminService.changePassword(email, currentPassword, newPassword);
        if (success) {
            return ResponseEntity.ok("Password updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect current password");
        }
    }
}
