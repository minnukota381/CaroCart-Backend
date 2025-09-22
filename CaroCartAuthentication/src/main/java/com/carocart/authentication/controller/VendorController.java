package com.carocart.authentication.controller;

import com.carocart.authentication.dto.VendorRegistrationDTO;
import com.carocart.authentication.dto.VendorResponseDTO;
import com.carocart.authentication.entity.Vendor;
import com.carocart.authentication.entity.VendorType;
import com.carocart.authentication.repository.VendorRepository;
import com.carocart.authentication.service.OtpEmailService;
import com.carocart.authentication.service.VendorService;
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

    @Autowired private VendorService vendorService;
    @Autowired private VendorRepository vendorRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private OtpEmailService otpEmailService;

    // ===== Signup with OTP =====
    @PostMapping("/signup/request-otp")
    public ResponseEntity<?> requestOtp(@Valid @RequestBody VendorRegistrationDTO dto) {
        try {
            String response = vendorService.requestSignupOtp(dto);
            return ResponseEntity.ok(Map.of("message", response));
        } catch (IllegalArgumentException ex) {
            // duplicates / bad input → 400 with clear message
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            // unexpected → 500 with a safer message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal error"));
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

    // ===== Login =====
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        try {
            String jwtToken = vendorService.loginVendor(email, password);
            return ResponseEntity.ok(jwtToken);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // ===== Vendor self profile =====
    @GetMapping("/me")
    public ResponseEntity<VendorResponseDTO> getCurrentVendor(@RequestHeader("Authorization") String token) {
        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        String email = jwtUtil.extractUsername(jwt);
        VendorResponseDTO dto = vendorService.getCurrentVendorProfile(email);
        return ResponseEntity.ok(dto);
    }

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

    @PutMapping("/me/profile-image-url")
    public ResponseEntity<String> updateVendorProfileImageUrl(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> body
    ) {
        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        String email = jwtUtil.extractUsername(jwt);
        String imageUrl = body.get("imageUrl");
        boolean success = vendorService.updateProfileImageUrl(email, imageUrl);
        return success
                ? ResponseEntity.ok("Vendor profile image URL updated successfully")
                : ResponseEntity.badRequest().body("Vendor not found");
    }

    // ===== Admin: vendor management =====
    @GetMapping("/admin/vendors/all")
    public ResponseEntity<List<VendorResponseDTO>> getAllVendors() {
        List<VendorResponseDTO> vendors = vendorService.getAllVendors().stream()
                .map(VendorService::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/admin/vendors/pending")
    public ResponseEntity<List<VendorResponseDTO>> getPendingVendors() {
        List<VendorResponseDTO> vendors = vendorService.getPendingVendors().stream()
                .map(VendorService::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(vendors);
    }

    @PostMapping("/admin/approve/{vendorId}")
    public ResponseEntity<String> approveVendor(@PathVariable Long vendorId) {
        boolean approved = vendorService.approveVendor(vendorId);
        if (approved) return ResponseEntity.ok("Vendor approved and notified");
        return ResponseEntity.badRequest().body("Vendor not found");
    }

    @PostMapping("/admin/reject/{vendorId}")
    public ResponseEntity<String> rejectVendor(@PathVariable Long vendorId) {
        boolean rejected = vendorService.rejectVendor(vendorId);
        if (rejected) return ResponseEntity.ok("Vendor rejected and removed from system");
        return ResponseEntity.badRequest().body("Vendor not found");
    }

    @DeleteMapping("/admin/vendors/{vendorId}")
    public ResponseEntity<String> deleteVendor(@PathVariable Long vendorId) {
        boolean deleted = vendorService.deleteVendorById(vendorId);
        if (deleted) return ResponseEntity.ok("Vendor deleted successfully");
        return ResponseEntity.badRequest().body("Vendor not found or could not be deleted");
    }

    @GetMapping("/admin/vendors/count")
    public ResponseEntity<Long> getTotalVendors() {
        long count = vendorRepository.count();
        return ResponseEntity.ok(count);
    }

    // ===== Password =====
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

    // ===== Public getters (by ID) =====
    @GetMapping("/{id}")
    public ResponseEntity<VendorResponseDTO> getVendorById(
            @PathVariable Long id,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng
    ) {
        Vendor vendor = vendorService.getVendorById(id);
        if (vendor == null || !vendor.isApproved()) {
            return ResponseEntity.status(404).body(null);
        }

        VendorResponseDTO dto = VendorService.toResponseDTO(vendor);

        // If caller provided location, reuse your existing Distance Matrix flow
        if (lat != null && lng != null) {
            var list = vendorService.findByTypeAndProximityDriving(
                    vendor.getVendorType(), lat, lng, /* big radius */ 5000
            );

            list.stream()
                .filter(x -> x.getId().equals(dto.getId()))
                .findFirst()
                .ifPresent(x -> {
                    dto.setDrivingKm(x.getDrivingKm());
                    dto.setDrivingMinutes(x.getDrivingMinutes());
                    dto.setEtaMinutes(x.getEtaMinutes());
                });
        }

        return ResponseEntity.ok(dto);
    }



    // ===== Slug endpoints (UPDATED) =====

    @GetMapping("/resolve/{vendorSlug:.+}")
    public ResponseEntity<Map<String, Object>> resolveBySlug(@PathVariable("vendorSlug") String vendorSlug) {
        Vendor v = vendorService.getVendorBySlug(vendorSlug);
        if (v == null || !v.isApproved()) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(Map.of(
                "id", v.getId(),
                "slug", v.getSlug(),
                "citySlug", v.getCitySlug(),
                "companyName", v.getCompanyName()
        ));
    }

    @GetMapping("/by-slug/{vendorSlug:.+}")
    public ResponseEntity<VendorResponseDTO> getBySlug(@PathVariable("vendorSlug") String vendorSlug) {
        Vendor v = vendorService.getVendorBySlug(vendorSlug);
        if (v == null || !v.isApproved()) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(VendorService.toResponseDTO(v));
    }

    @GetMapping("/city/{citySlug:.+}/{vendorSlug:.+}")
    public ResponseEntity<VendorResponseDTO> getByCityAndSlug(@PathVariable("citySlug") String citySlug,
                                                              @PathVariable("vendorSlug") String vendorSlug) {
        Vendor v = vendorService.getVendorByCityAndSlug(citySlug, vendorSlug);
        if (v == null || !v.isApproved()) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(VendorService.toResponseDTO(v));
    }

    // ===== Nearby (driving distance) =====
    @GetMapping("/nearby")
    public ResponseEntity<List<VendorResponseDTO>> getNearbyVendors(
            @RequestParam VendorType type,
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "12") double radiusKm
    ) {
        var dtos = vendorService.findByTypeAndProximityDriving(type, lat, lng, radiusKm);
        return ResponseEntity.ok(dtos);
    }

    // ===== Batch fetch by IDs =====
    @PostMapping("/by-ids")
    public ResponseEntity<List<VendorResponseDTO>> getVendorsByIds(@RequestBody List<Long> vendorIds) {
        if (vendorIds == null || vendorIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<Vendor> vendors = vendorService.getVendorsByIds(vendorIds);
        List<VendorResponseDTO> dtos = vendors.stream()
                .filter(Vendor::isApproved)
                .map(VendorService::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // ===== Availability toggle =====
    @PutMapping("/{id}/availability")
    public ResponseEntity<?> updateAvailability(
            @PathVariable Long id,
            @RequestParam boolean acceptingOrders
    ) {
        try {
            VendorResponseDTO updatedVendor = vendorService.updateAvailability(id, acceptingOrders);
            return ResponseEntity.ok(updatedVendor);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // ===== Public search for restaurants =====
    @GetMapping("/search")
    public ResponseEntity<List<VendorResponseDTO>> searchRestaurants(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<Vendor> vendors = vendorService.searchRestaurants(q, limit);
        List<VendorResponseDTO> dtos = vendors.stream()
                .map(VendorService::toResponseDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // ===== Public: lookup by email =====
    @GetMapping("/email/{email}")
    public ResponseEntity<VendorResponseDTO> getVendorByEmailPublic(@PathVariable String email) {
        var v = vendorService.getVendorByEmail(email);
        if (v == null || !v.isApproved()) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(VendorService.toResponseDTO(v));
    }
}
