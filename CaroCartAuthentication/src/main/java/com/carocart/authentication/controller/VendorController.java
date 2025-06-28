package com.carocart.authentication.controller;

import com.carocart.authentication.dto.VendorResponseDTO;
import com.carocart.authentication.entity.Vendor;
import com.carocart.authentication.repository.VendorRepository;
import com.carocart.authentication.service.VendorService;
import com.carocart.authentication.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/vendors")
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // --- Signup with OTP ---
    @PostMapping("/signup/request-otp")
    public ResponseEntity<String> requestOtp(@RequestBody Vendor vendor) {
        try {
            String response = vendorService.requestSignupOtp(vendor);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/signup/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        try {
            String response = vendorService.verifySignupOtp(email, otp);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // --- Login ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        try {
            String jwtToken = vendorService.loginVendor(email, password);
            return ResponseEntity.ok(jwtToken);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // --- Get current vendor profile ---
    @GetMapping("/me")
    public ResponseEntity<VendorResponseDTO> getCurrentVendor(@RequestHeader("Authorization") String token) {
        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        String email = jwtUtil.extractUsername(jwt);

        Vendor vendor = vendorService.getVendorByEmail(email);
        if (vendor == null || !vendor.isApproved()) {
            return ResponseEntity.status(403).body(null);
        }

        VendorResponseDTO dto = new VendorResponseDTO();
        dto.setId(vendor.getId());
        dto.setFirstName(vendor.getFirstName());
        dto.setLastName(vendor.getLastName());
        dto.setEmail(vendor.getEmail());
        dto.setRole("VENDOR");
        dto.setCompanyName(vendor.getCompanyName());
        dto.setContactNumber(vendor.getContactNumber());
        dto.setGstNumber(vendor.getGstNumber());
        dto.setBusinessType(vendor.getBusinessType());
        dto.setBusinessAddress(vendor.getBusinessAddress());
        dto.setProfileImage(vendor.getProfileImage()); // Base64 or raw byte[] depending on frontend handling
        return ResponseEntity.ok(dto);
    }

    // --- Update vendor profile ---
    @PutMapping("/me")
    public ResponseEntity<String> updateCurrentVendor(@RequestHeader("Authorization") String token,
                                                      @RequestBody VendorResponseDTO updatedVendor) {
        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        String email = jwtUtil.extractUsername(jwt);

        try {
            Vendor updated = new Vendor();
            updated.setFirstName(updatedVendor.getFirstName());
            updated.setLastName(updatedVendor.getLastName());
            updated.setCompanyName(updatedVendor.getCompanyName());
            updated.setContactNumber(updatedVendor.getContactNumber());
            updated.setGstNumber(updatedVendor.getGstNumber());
            updated.setBusinessType(updatedVendor.getBusinessType());
            updated.setBusinessAddress(updatedVendor.getBusinessAddress());

            vendorService.updateVendorProfile(email, updated);
            return ResponseEntity.ok("Vendor profile updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating vendor profile");
        }
    }

    // --- Upload profile image ---
    @PostMapping("/me/profile-image")
    public ResponseEntity<?> uploadProfileImage(@RequestHeader("Authorization") String token,
                                                @RequestPart("profileImage") MultipartFile file) {
        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        String email = jwtUtil.extractUsername(jwt);

        try {
            vendorService.uploadProfileImage(email, file);
            return ResponseEntity.ok(Map.of("message", "Image uploaded successfully"));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error uploading image");
        }
    }

    // --- Admin: Get all vendors ---
    @GetMapping("/admin/vendors/all")
    public ResponseEntity<List<Vendor>> getAllVendors() {
        return ResponseEntity.ok(vendorService.getAllVendors());
    }

    // --- Admin: Get pending vendors ---
    @GetMapping("/admin/vendors/pending")
    public ResponseEntity<List<Vendor>> getPendingVendors() {
        return ResponseEntity.ok(vendorService.getPendingVendors());
    }

    // --- Admin: Approve vendor ---
    @PostMapping("/admin/approve/{vendorId}")
    public ResponseEntity<String> approveVendor(@PathVariable Long vendorId) {
        boolean approved = vendorService.approveVendor(vendorId);
        if (approved) {
            return ResponseEntity.ok("Vendor approved and notified");
        } else {
            return ResponseEntity.badRequest().body("Vendor not found");
        }
    }

    // --- Admin: Reject vendor ---
    @PostMapping("/admin/reject/{vendorId}")
    public ResponseEntity<String> rejectVendor(@PathVariable Long vendorId) {
        boolean rejected = vendorService.rejectVendor(vendorId);
        if (rejected) {
            return ResponseEntity.ok("Vendor rejected and removed from system");
        } else {
            return ResponseEntity.badRequest().body("Vendor not found");
        }
    }
 // --- Admin: Delete vendor ---
    @DeleteMapping("/admin/vendors/{vendorId}")
    public ResponseEntity<String> deleteVendor(@PathVariable Long vendorId) {
        boolean deleted = vendorService.deleteVendorById(vendorId);
        if (deleted) {
            return ResponseEntity.ok("Vendor deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("Vendor not found or could not be deleted");
        }
    }
    
 // ✅ Get total vendor count (for admin dashboard/stat card)
    @GetMapping("/admin/vendors/count")
    public ResponseEntity<Long> getTotalVendors() {
        long count = vendorRepository.count();
        return ResponseEntity.ok(count);
    }

    
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
    
 // --- Internal: Get vendor by ID (used by ProductService/OrderService via Feign) ---
    @GetMapping("/{id}")
    public ResponseEntity<VendorResponseDTO> getVendorById(@PathVariable Long id) {
        Vendor vendor = vendorService.getVendorById(id);
        if (vendor == null || !vendor.isApproved()) {
            return ResponseEntity.status(404).body(null);
        }

        VendorResponseDTO dto = new VendorResponseDTO();
        dto.setId(vendor.getId());
        dto.setFirstName(vendor.getFirstName());
        dto.setLastName(vendor.getLastName());
        dto.setEmail(vendor.getEmail());
        dto.setRole("VENDOR");
        dto.setCompanyName(vendor.getCompanyName());
        dto.setContactNumber(vendor.getContactNumber());
        dto.setGstNumber(vendor.getGstNumber());
        dto.setBusinessType(vendor.getBusinessType());
        dto.setBusinessAddress(vendor.getBusinessAddress());
        dto.setProfileImage(vendor.getProfileImage());
        return ResponseEntity.ok(dto);
    }



}
