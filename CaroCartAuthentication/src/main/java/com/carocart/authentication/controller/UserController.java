// package and imports unchanged
package com.carocart.authentication.controller;

import com.carocart.authentication.dto.UserRegistrationDTO;
import com.carocart.authentication.dto.UserDTO;
import com.carocart.authentication.entity.User;
import com.carocart.authentication.repository.UserRepository;
import com.carocart.authentication.service.OtpEmailService;
import com.carocart.authentication.service.UserService;
import com.carocart.authentication.util.JwtUtil;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired private UserService userService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepository;
    @Autowired private OtpEmailService otpEmailService;

    // === USER REGISTRATION ===
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody UserRegistrationDTO dto) {
        String result = userService.signup(dto);
        return "User registered successfully".equals(result)
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    // === LOGIN ===
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        try {
            String token = userService.login(user.getEmail(), user.getPassword());
            return ResponseEntity.ok(token);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // === GET CURRENT USER BY TOKEN ===
 // === GET CURRENT USER BY TOKEN ===
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authorizationHeader.substring(7);
        String role = jwtUtil.extractRole(token);

        if ("SYSTEM".equalsIgnoreCase(role)) {
            UserDTO systemUser = new UserDTO();
            systemUser.setId(0L);
            systemUser.setEmail("internal-service@carocart.com");
            systemUser.setRole("SYSTEM");
            systemUser.setFirstName("System");
            return ResponseEntity.ok(systemUser);
        }

        String email = jwtUtil.extractUsername(token);
        return userService.findByEmail(email)
            .map(user -> {
                UserDTO dto = new UserDTO();
                dto.setId(user.getId());
                dto.setEmail(user.getEmail());
                dto.setRole(user.getRole());
                dto.setFirstName(user.getFirstName());
                dto.setLastName(user.getLastName());
                // ✅ ensure +91 prefix if missing
                String raw = user.getPhoneNumber();   // was getContactNumber()
                if (raw != null && !raw.isBlank()) {
                    dto.setMobileNumber(raw.startsWith("+") ? raw : "+91" + raw);
                }
                return ResponseEntity.ok(dto);
            })
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // === GET USER BY ID (for FEIGN) ===
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getCurrentUserById(@PathVariable Long id) {
        return userRepository.findById(id)
            .map(user -> {
                UserDTO dto = new UserDTO();
                dto.setId(user.getId());
                dto.setEmail(user.getEmail());
                dto.setRole(user.getRole());
                dto.setFirstName(user.getFirstName());
                dto.setLastName(user.getLastName());
                // ✅ ensure +91 prefix if missing
                String raw = user.getPhoneNumber();   // was getContactNumber()
                if (raw != null && !raw.isBlank()) {
                    dto.setMobileNumber(raw.startsWith("+") ? raw : "+91" + raw);
                }
                return ResponseEntity.ok(dto);
            })
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }


    // === UPDATE USER PROFILE ===
    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody User updatedUser
    ) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Authorization header");
        }
        String token = authorizationHeader.substring(7);
        String email = jwtUtil.extractUsername(token);
        boolean success = userService.updateUserProfile(email, updatedUser);
        return success
                ? ResponseEntity.ok("Profile updated successfully")
                : ResponseEntity.badRequest().body("Update failed");
    }

    // === UPDATE PROFILE IMAGE ===
    @PutMapping("/profile/image-url")
    public ResponseEntity<String> updateProfileImageUrl(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);
        String imageUrl = body.get("imageUrl");
        boolean success = userService.updateProfileImageUrl(email, imageUrl);
        return success
                ? ResponseEntity.ok("Profile image URL updated successfully")
                : ResponseEntity.badRequest().body("User not found");
    }

    // === GET PROFILE ===
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authorizationHeader.substring(7);
        String email = jwtUtil.extractUsername(token);
        return userService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // === UPDATE LOCATION (by user) ===
    @PutMapping("/me/location")
    public ResponseEntity<String> updateUserLocation(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Double> location
    ) {
        String email = jwtUtil.extractUsername(token.substring(7));
        Optional<User> optionalUser = userService.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User user = optionalUser.get();
        userService.save(user);
        return ResponseEntity.ok("Location updated successfully");
    }

    // === PASSWORD FLOW ===
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (userRepository.findByEmail(email).isEmpty()) {
            return ResponseEntity.badRequest().body("Email not registered");
        }
        String otp = otpEmailService.generateOtp();
        otpEmailService.storeOtp(email, otp);
        otpEmailService.sendOtpEmail(email, otp);
        return ResponseEntity.ok("OTP sent to email");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        boolean isValid = otpEmailService.validateOtp(email, otp);
        if (isValid) {
            otpEmailService.removeOtp(email);
            return ResponseEntity.ok("OTP verified");
        }
        return ResponseEntity.badRequest().body("Invalid OTP");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");
        boolean success = userService.updatePassword(email, newPassword);
        return success
                ? ResponseEntity.ok("Password updated successfully")
                : ResponseEntity.badRequest().body("Failed to update password");
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> passwordMap
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);
        String currentPassword = passwordMap.get("currentPassword");
        String newPassword = passwordMap.get("newPassword");
        boolean success = userService.changePassword(email, currentPassword, newPassword);
        return success
                ? ResponseEntity.ok("Password changed successfully")
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Current password is incorrect");
    }

    // === ADMIN ===
    @GetMapping("/admin/users/count")
    public ResponseEntity<Long> getUserCount() {
        return ResponseEntity.ok(userRepository.count());
    }

    @GetMapping("/admin/users/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/admin/users/{id}")
    public ResponseEntity<String> updateUserByAdmin(@PathVariable Long id, @RequestBody User updatedUser) {
        return userRepository.findById(id).map(user -> {
            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            user.setEmail(updatedUser.getEmail());
            user.setRole(updatedUser.getRole());
            userRepository.save(user);
            return ResponseEntity.ok("User updated successfully");
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    // === INTERNAL: UPDATE USER LOCATION (SYSTEM ONLY)
    // NOTE: Controller is @RequestMapping("/users"), so the final path is:
    //       PATCH /users/internal/{id}/location
    @PatchMapping("/internal/{id}/location")
    public ResponseEntity<String> updateUserLocationInternal(
        @RequestHeader("Authorization") String authHeader,
        @PathVariable Long id,
        @RequestBody Map<String, Double> location
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String token = authHeader.substring(7);
        String role = jwtUtil.extractRole(token);
        if (!"SYSTEM".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }

        return userRepository.findById(id).map(user -> {
            Double lat = location.get("latitude");
            Double lng = location.get("longitude");
            userService.saveAndFlush(user); // ✅ ensure immediate DB write
            System.out.println("✅ Updated user " + id + " -> lat=" + lat + ", lng=" + lng);
            return ResponseEntity.ok("User location updated");
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }


}
