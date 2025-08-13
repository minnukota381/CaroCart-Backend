package com.carocart.authentication.service;

import com.carocart.authentication.dto.VendorRegistrationDTO;
import com.carocart.authentication.dto.VendorResponseDTO;
import com.carocart.authentication.entity.Vendor;
import com.carocart.authentication.entity.VendorType;
import com.carocart.authentication.repository.VendorRepository;
import com.carocart.authentication.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class VendorService {

    @Autowired
    private VendorRepository vendorRepository;
    @Autowired
    private OtpEmailService otpEmailService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    private final Map<String, Vendor> pendingVendors = new ConcurrentHashMap<>();

    // 1. Request OTP for signup (now uses DTO & validation)
    public String requestSignupOtp(VendorRegistrationDTO dto) {
        if (vendorRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        Vendor vendor = fromRegistrationDTO(dto);
        vendor.setPassword(dto.getPassword()); // Will encode after OTP
        pendingVendors.put(dto.getEmail(), vendor);

        String otp = otpEmailService.generateOtp();
        otpEmailService.storeOtp(dto.getEmail(), otp);
        otpEmailService.sendOtpEmail(dto.getEmail(), otp);
        return "OTP sent to your email";
    }

    // 2. Verify OTP and complete registration
    public String verifySignupOtp(String email, String otp) {
        if (!otpEmailService.validateOtp(email, otp)) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        Vendor vendor = pendingVendors.get(email);
        if (vendor == null) {
            throw new IllegalStateException("No pending signup found for this email");
        }
        vendor.setPassword(passwordEncoder.encode(vendor.getPassword()));
        vendor.setApproved(false);
        vendorRepository.save(vendor);

        otpEmailService.removeOtp(email);
        pendingVendors.remove(email);

        return "Vendor registered successfully, pending admin approval";
    }

    // 3. Vendor Login with JWT token (only if approved)
    public String loginVendor(String email, String password) {
        Optional<Vendor> vendorOpt = vendorRepository.findByEmail(email);
        if (vendorOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        Vendor vendor = vendorOpt.get();
        if (!passwordEncoder.matches(password, vendor.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        if (!vendor.isApproved()) {
            throw new IllegalStateException("Vendor not approved yet");
        }
        return jwtUtil.generateToken(
                vendor.getId(),
                vendor.getEmail(),
                vendor.getRole(),
                vendor.getFirstName(),
                vendor.getLastName()
        );
    }

    // 4. Admin approval of vendor
    public boolean approveVendor(Long vendorId) {
        Optional<Vendor> optionalVendor = vendorRepository.findById(vendorId);
        if (optionalVendor.isPresent()) {
            Vendor vendor = optionalVendor.get();
            vendor.setApproved(true);
            vendorRepository.save(vendor);
            otpEmailService.sendApprovalEmail(vendor.getEmail());
            return true;
        }
        return false;
    }

    public boolean rejectVendor(Long vendorId) {
        Optional<Vendor> optionalVendor = vendorRepository.findById(vendorId);
        if (optionalVendor.isPresent()) {
            Vendor vendor = optionalVendor.get();
            vendorRepository.delete(vendor);
            otpEmailService.sendRejectionEmail(vendor.getEmail());
            return true;
        }
        return false;
    }

    public boolean updateProfileImageUrl(String email, String imageUrl) {
        Vendor vendor = getVendorByEmail(email);
        if (vendor != null) {
            vendor.setProfileImageUrl(imageUrl);
            vendorRepository.save(vendor);
            return true;
        }
        return false;
    }

    // Utility method: get vendor by ID
    public Vendor getVendorById(Long id) {
        return vendorRepository.findById(id).orElse(null);
    }

    public List<Vendor> getPendingVendors() {
        return vendorRepository.findByIsApprovedFalse();
    }

    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    public Vendor getVendorByEmail(String email) {
        return vendorRepository.findByEmail(email).orElse(null);
    }

    // Get vendor profile (by JWT email)
    public VendorResponseDTO getCurrentVendorProfile(String email) {
        Vendor vendor = vendorRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found"));
        return toResponseDTO(vendor);
    }

    // Update vendor profile (excluding email & password)
    public Vendor updateVendorProfile(String email, VendorResponseDTO updated) {
        Vendor existing = getVendorByEmail(email);
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setCompanyName(updated.getCompanyName());
        existing.setContactNumber(updated.getContactNumber());
        existing.setGstNumber(updated.getGstNumber());
        existing.setVendorType(updated.getVendorType());
        existing.setAddressLine1(updated.getAddressLine1());
        existing.setAddressLine2(updated.getAddressLine2());
        existing.setCity(updated.getCity());
        existing.setState(updated.getState());
        existing.setPinCode(updated.getPinCode());
        existing.setLatitude(updated.getLatitude());
        existing.setLongitude(updated.getLongitude());
        return vendorRepository.save(existing);
    }

    // Upload profile image (now just URL)
    public void uploadProfileImage(String email, MultipartFile file) throws IOException {
        // This can be refactored or removed if you only store URLs!
        throw new UnsupportedOperationException("Only profile image URL is supported. Upload to Firebase/S3 and save the URL.");
    }

    public boolean deleteVendorById(Long vendorId) {
        Optional<Vendor> vendorOptional = vendorRepository.findById(vendorId);
        if (vendorOptional.isPresent()) {
            vendorRepository.deleteById(vendorId);
            return true;
        }
        return false;
    }

    public void changePassword(String email, String currentPassword, String newPassword) {
        Vendor vendor = getVendorByEmail(email);
        if (!passwordEncoder.matches(currentPassword, vendor.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        vendor.setPassword(passwordEncoder.encode(newPassword));
        vendorRepository.save(vendor);
    }

    // ====== NEW: FIND BY TYPE AND PROXIMITY ======
    /**
     * Find vendors of a given type within a radius (in km) of a point.
     * For MVP: filters in Java. For production: use spatial query in repository.
     */
    public List<Vendor> findByTypeAndProximity(VendorType type, double lat, double lng, double radiusKm) {
        List<Vendor> all = vendorRepository.findByVendorTypeAndIsApproved(type, true);
        return all.stream()
            .filter(v -> haversineDistanceKm(lat, lng, v.getLatitude(), v.getLongitude()) <= radiusKm)
            .collect(Collectors.toList());
    }

    // Haversine formula for distance between 2 geo points (km)
    public static double haversineDistanceKm(double lat1, double lng1, double lat2, double lng2) {
        final int EARTH_RADIUS = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return EARTH_RADIUS * c;
    }

    // ===== DTO Mapping Helpers =====
    public static Vendor fromRegistrationDTO(VendorRegistrationDTO dto) {
        Vendor vendor = new Vendor();
        vendor.setFirstName(dto.getFirstName());
        vendor.setLastName(dto.getLastName());
        vendor.setCompanyName(dto.getCompanyName());
        vendor.setEmail(dto.getEmail());
        vendor.setContactNumber(dto.getContactNumber());
        vendor.setGstNumber(dto.getGstNumber());
        vendor.setVendorType(dto.getVendorType());
        vendor.setAddressLine1(dto.getAddressLine1());
        vendor.setAddressLine2(dto.getAddressLine2());
        vendor.setCity(dto.getCity());
        vendor.setState(dto.getState());
        vendor.setPinCode(dto.getPinCode());
        vendor.setLatitude(dto.getLatitude());
        vendor.setLongitude(dto.getLongitude());
        vendor.setRole("VENDOR");
        return vendor;
    }
    public static VendorResponseDTO toResponseDTO(Vendor vendor) {
        VendorResponseDTO dto = new VendorResponseDTO();
        dto.setId(vendor.getId());
        dto.setFirstName(vendor.getFirstName());
        dto.setLastName(vendor.getLastName());
        dto.setEmail(vendor.getEmail());
        dto.setRole(vendor.getRole());
        dto.setCompanyName(vendor.getCompanyName());
        dto.setContactNumber(vendor.getContactNumber());
        dto.setGstNumber(vendor.getGstNumber());
        dto.setVendorType(vendor.getVendorType());
        dto.setAddressLine1(vendor.getAddressLine1());
        dto.setAddressLine2(vendor.getAddressLine2());
        dto.setCity(vendor.getCity());
        dto.setState(vendor.getState());
        dto.setPinCode(vendor.getPinCode());
        dto.setLatitude(vendor.getLatitude());
        dto.setLongitude(vendor.getLongitude());
        dto.setProfileImageUrl(vendor.getProfileImageUrl());
        dto.setApproved(vendor.isApproved()); 
        dto.setUpiId(vendor.getUpiId());// <--- Add this line
        return dto;
    }
    
    
    public void updatePassword(String email, String newPassword) {
        Vendor v = vendorRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Vendor not found"));

        v.setPassword(passwordEncoder.encode(newPassword));
        vendorRepository.save(v);
    }


}
