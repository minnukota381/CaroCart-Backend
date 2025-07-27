package com.carocart.authentication.controller;

import com.carocart.authentication.dto.VendorRegistrationDTO;
import com.carocart.authentication.dto.VendorResponseDTO;
import com.carocart.authentication.entity.Vendor;
import com.carocart.authentication.entity.VendorType;
import com.carocart.authentication.service.VendorService;
import com.carocart.authentication.repository.VendorRepository;
import com.carocart.authentication.util.JwtUtil;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/vendors")
public class VendorController {

    @Autowired
    private VendorService vendorService;
    @Autowired
    private VendorRepository vendorRepository;
    @Autowired
    private JwtUtil jwtUtil;

    // --- SIGNUP WITH OTP (DTO + Validation) ---
    @PostMapping("/signup/request-otp")
    public ResponseEntity<String> requestOtp(@Valid @RequestBody VendorRegistrationDTO dto) {
        try {
            String response = vendorService.requestSignupOtp(dto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // --- VERIFY OTP ---
    @PostMapping("/signup/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        try {
            String response = vendorService.verifySignupOtp(email, otp);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // --- LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        try {
            String jwtToken = vendorService.loginVendor(email, password);
            return ResponseEntity.ok(jwtToken);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // --- GET CURRENT VENDOR PROFILE ---
    @GetMapping("/me")
    public ResponseEntity<VendorResponseDTO> getCurrentVendor(@RequestHeader("Authorization") String token) {
        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        String email = jwtUtil.extractUsername(jwt);
        VendorResponseDTO dto = vendorService.getCurrentVendorProfile(email);
        return ResponseEntity.ok(dto);
    }

    // --- UPDATE CURRENT VENDOR PROFILE ---
    @PutMapping("/me")
    public ResponseEntity<String> updateCurrentVendor(@RequestHeader("Authorization") String token,
                                                      @RequestBody VendorResponseDTO updatedVendor) {
        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        String email = jwtUtil.extractUsername(jwt);
        try {
            vendorService.updateVendorProfile(email, updatedVendor);
            return ResponseEntity.ok("Vendor profile updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating vendor profile");
        }
    }

    // --- SAVE/UPDATE PROFILE IMAGE URL ---
    @PutMapping("/me/profile-image-url")
    public ResponseEntity<String> updateVendorProfileImageUrl(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> body) {
        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        String email = jwtUtil.extractUsername(jwt);
        String imageUrl = body.get("imageUrl");
        boolean success = vendorService.updateProfileImageUrl(email, imageUrl);
        return success
                ? ResponseEntity.ok("Vendor profile image URL updated successfully")
                : ResponseEntity.badRequest().body("Vendor not found");
    }

    // --- ADMIN: Get all vendors ---
    @GetMapping("/admin/vendors/all")
    public ResponseEntity<List<VendorResponseDTO>> getAllVendors() {
        List<VendorResponseDTO> vendors = vendorService.getAllVendors().stream()
                .map(VendorService::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(vendors);
    }

    // --- ADMIN: Get pending vendors ---
    @GetMapping("/admin/vendors/pending")
    public ResponseEntity<List<VendorResponseDTO>> getPendingVendors() {
        List<VendorResponseDTO> vendors = vendorService.getPendingVendors().stream()
                .map(VendorService::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(vendors);
    }

    // --- ADMIN: Approve vendor ---
    @PostMapping("/admin/approve/{vendorId}")
    public ResponseEntity<String> approveVendor(@PathVariable Long vendorId) {
        boolean approved = vendorService.approveVendor(vendorId);
        if (approved) {
            return ResponseEntity.ok("Vendor approved and notified");
        } else {
            return ResponseEntity.badRequest().body("Vendor not found");
        }
    }

    // --- ADMIN: Reject vendor ---
    @PostMapping("/admin/reject/{vendorId}")
    public ResponseEntity<String> rejectVendor(@PathVariable Long vendorId) {
        boolean rejected = vendorService.rejectVendor(vendorId);
        if (rejected) {
            return ResponseEntity.ok("Vendor rejected and removed from system");
        } else {
            return ResponseEntity.badRequest().body("Vendor not found");
        }
    }

    // --- ADMIN: Delete vendor ---
    @DeleteMapping("/admin/vendors/{vendorId}")
    public ResponseEntity<String> deleteVendor(@PathVariable Long vendorId) {
        boolean deleted = vendorService.deleteVendorById(vendorId);
        if (deleted) {
            return ResponseEntity.ok("Vendor deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("Vendor not found or could not be deleted");
        }
    }

    // --- ADMIN: Get vendor count ---
    @GetMapping("/admin/vendors/count")
    public ResponseEntity<Long> getTotalVendors() {
        long count = vendorRepository.count();
        return ResponseEntity.ok(count);
    }

    // --- CHANGE PASSWORD ---
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> passwords
    ) {
        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        String email = jwtUtil.extractUsername(jwt);
        String currentPassword = passwords.get("currentPassword");
        String newPassword = passwords.get("newPassword");
        try {
            vendorService.changePassword(email, currentPassword, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "An error occurred"));
        }
    }

    // --- GET VENDOR BY ID (for Feign) ---
    @GetMapping("/{id}")
    public ResponseEntity<VendorResponseDTO> getVendorById(@PathVariable Long id) {
        Vendor vendor = vendorService.getVendorById(id);
        if (vendor == null || !vendor.isApproved()) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(VendorService.toResponseDTO(vendor));
    }

    // === SCALABLE: VENDORS NEARBY BY TYPE AND PROXIMITY ===
    // GET /vendors/nearby?type=FOOD&lat=..&lng=..&radius=10
    @GetMapping("/nearby")
    public ResponseEntity<List<VendorResponseDTO>> getNearbyVendors(
            @RequestParam VendorType type,
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "10") double radiusKm
    ) {
        List<Vendor> vendors = vendorService.findByTypeAndProximity(type, lat, lng, radiusKm);
        List<VendorResponseDTO> dtos = vendors.stream()
                .map(VendorService::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
