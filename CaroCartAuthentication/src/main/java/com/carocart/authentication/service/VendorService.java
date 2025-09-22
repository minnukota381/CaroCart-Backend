package com.carocart.authentication.service;

import com.carocart.authentication.dto.DrivingInfo;
import com.carocart.authentication.dto.VendorRegistrationDTO;
import com.carocart.authentication.dto.VendorResponseDTO;
import com.carocart.authentication.entity.Vendor;
import com.carocart.authentication.entity.VendorType;
import com.carocart.authentication.repository.VendorRepository;
import com.carocart.authentication.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VendorService {

    @Autowired private VendorRepository vendorRepository;
    @Autowired private OtpEmailService otpEmailService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private DistanceMatrixService distanceMatrixService;

    // Slug generator (create + update)
    @Autowired private VendorSlugService vendorSlugService;

    private final Map<String, Vendor> pendingVendors = new ConcurrentHashMap<>();

    // =========================
    // Signup (OTP Disabled)
    // =========================
    public String requestSignupOtp(VendorRegistrationDTO dto) {
        // ---- pre-insert duplicate checks (for nicer error messages) ----
        if (vendorRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (StringUtils.hasText(dto.getContactNumber()) &&
                vendorRepository.existsByContactNumber(dto.getContactNumber())) {
            throw new IllegalArgumentException("Contact number already registered");
        }
        if (StringUtils.hasText(dto.getGstNumber()) &&
                vendorRepository.existsByGstNumber(dto.getGstNumber())) {
            throw new IllegalArgumentException("GST number already registered");
        }
        if (StringUtils.hasText(dto.getCompanyName()) &&
                vendorRepository.existsByCompanyName(dto.getCompanyName())) {
            throw new IllegalArgumentException("Company name already registered");
        }

        // ---- create entity ----
        Vendor vendor = fromRegistrationDTO(dto);
        vendor.setPassword(passwordEncoder.encode(dto.getPassword()));
        vendor.setApproved(false);

        // ✅ generate citySlug + slug BEFORE first save
        vendorSlugService.ensureSlugs(vendor);

        try {
            vendorRepository.save(vendor);
        } catch (DataIntegrityViolationException ex) {
            // safety net in case of any DB unique constraint violation
            throw new IllegalArgumentException("Duplicate value on a unique field (email/contact/gst/company/slug)");
        }

        // ❌ OTP flow disabled but kept for future use
        /*
        pendingVendors.put(dto.getEmail(), vendor);
        String otp = otpEmailService.generateOtp();
        otpEmailService.storeOtp(dto.getEmail(), otp);
        otpEmailService.sendOtpEmail(dto.getEmail(), otp);
        return "OTP sent to your email";
        */

        return "Vendor registered successfully, pending admin approval";
    }

    public String verifySignupOtp(String email, String otp) {
        // OTP disabled for now
        throw new UnsupportedOperationException("OTP verification is disabled for now");
    }

    // =========================
    // Login
    // =========================
    public String loginVendor(String email, String password) {
        Optional<Vendor> vendorOpt = vendorRepository.findByEmail(email);
        if (vendorOpt.isEmpty()) throw new IllegalArgumentException("Invalid email or password");

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

    // =========================
    // Slug-based fetchers
    // =========================
    public Vendor getVendorBySlug(String slug) {
        return vendorRepository.findBySlug(slug).orElse(null);
    }

    public Vendor getVendorByCityAndSlug(String citySlug, String slug) {
        return vendorRepository.findByCitySlugAndSlug(citySlug, slug).orElse(null);
    }

    // =========================
    // Admin approvals
    // =========================
    public boolean approveVendor(Long vendorId) {
        return vendorRepository.findById(vendorId).map(v -> {
            v.setApproved(true);
            vendorRepository.save(v);
            otpEmailService.sendApprovalEmail(v.getEmail());
            return true;
        }).orElse(false);
    }

    public boolean rejectVendor(Long vendorId) {
        return vendorRepository.findById(vendorId).map(v -> {
            vendorRepository.delete(v);
            otpEmailService.sendRejectionEmail(v.getEmail());
            return true;
        }).orElse(false);
    }

    // =========================
    // Profiles
    // =========================
    public boolean updateProfileImageUrl(String email, String imageUrl) {
        Vendor vendor = getVendorByEmail(email);
        if (vendor != null) {
            vendor.setProfileImageUrl(imageUrl);
            vendorRepository.save(vendor);
            return true;
        }
        return false;
    }

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

    public VendorResponseDTO getCurrentVendorProfile(String email) {
        Vendor vendor = vendorRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found"));
        return toResponseDTO(vendor);
    }

    public Vendor updateVendorProfile(String email, VendorResponseDTO updated) {
        Vendor existing = getVendorByEmail(email);
        if (existing == null) {
            throw new IllegalArgumentException("Vendor not found");
        }

        // snapshot essential fields for change detection
        Vendor before = new Vendor();
        before.setCompanyName(existing.getCompanyName());
        before.setCity(existing.getCity());

        // apply updates
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setCompanyName(updated.getCompanyName());
        existing.setContactNumber(updated.getContactNumber());
        existing.setAlternatePhone(updated.getAlternatePhone());
        existing.setGstNumber(updated.getGstNumber());
        existing.setVendorType(updated.getVendorType());
        existing.setAddressLine1(updated.getAddressLine1());
        existing.setAddressLine2(updated.getAddressLine2());
        existing.setCity(updated.getCity());
        existing.setState(updated.getState());
        existing.setPinCode(updated.getPinCode());
        existing.setLatitude(updated.getLatitude());
        existing.setLongitude(updated.getLongitude());
        existing.setOpeningTime(updated.getOpeningTime());
        existing.setClosingTime(updated.getClosingTime());
        existing.setEveningOpeningTime(updated.getEveningOpeningTime());
        existing.setEveningClosingTime(updated.getEveningClosingTime());

        // ✅ recompute slugs if company/city changed (or if missing)
        vendorSlugService.ensureSlugsOnUpdate(before, existing);

        return vendorRepository.save(existing);
    }

    public void uploadProfileImage(String email, MultipartFile file) throws IOException {
        throw new UnsupportedOperationException("Only profile image URL is supported.");
    }

    public boolean deleteVendorById(Long vendorId) {
        return vendorRepository.findById(vendorId).map(v -> {
            vendorRepository.deleteById(vendorId);
            return true;
        }).orElse(false);
    }

    public void changePassword(String email, String currentPassword, String newPassword) {
        Vendor vendor = getVendorByEmail(email);
        if (vendor == null) throw new IllegalArgumentException("Vendor not found");
        if (!passwordEncoder.matches(currentPassword, vendor.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        vendor.setPassword(passwordEncoder.encode(newPassword));
        vendorRepository.save(vendor);
    }

    // =========================
    // Nearby (road distance)
    // =========================
    public List<VendorResponseDTO> findByTypeAndProximityDriving(
            VendorType type, double lat, double lng, double radiusKm) {

        List<Vendor> candidates = vendorRepository.findByVendorTypeAndIsApproved(type, true)
                .stream()
                .filter(v -> v.getLatitude() != null && v.getLongitude() != null)
                .toList();

        if (candidates.isEmpty()) return Collections.emptyList();

        Map<Long, DrivingInfo> drivingMap = distanceMatrixService.drivingForVendors(lat, lng, candidates);

        return candidates.stream()
                .map(v -> {
                    VendorResponseDTO dto = toResponseDTO(v);
                    DrivingInfo info = drivingMap.get(v.getId());
                    if (info != null) {
                        dto.setDrivingKm(info.getKm());
                        dto.setEtaMinutes(info.getMinutes());
                        dto.setDrivingMinutes(info.getMinutes());
                    }
                    return dto;
                })
                .filter(dto -> dto.getDrivingKm() != null && dto.getDrivingKm() <= radiusKm)
                .toList();
    }

    // =========================
    // Utils
    // =========================
    public static Vendor fromRegistrationDTO(VendorRegistrationDTO dto) {
        Vendor vendor = new Vendor();
        vendor.setFirstName(dto.getFirstName());
        vendor.setLastName(dto.getLastName());
        vendor.setCompanyName(dto.getCompanyName());
        vendor.setEmail(dto.getEmail());
        vendor.setContactNumber(dto.getContactNumber());
        vendor.setAlternatePhone(dto.getAlternatePhone());
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
        vendor.setOpeningTime(dto.getOpeningTime());
        vendor.setClosingTime(dto.getClosingTime());
        vendor.setEveningOpeningTime(dto.getEveningOpeningTime());
        vendor.setEveningClosingTime(dto.getEveningClosingTime());
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
        dto.setAlternatePhone(vendor.getAlternatePhone());
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
        dto.setUpiId(vendor.getUpiId());
        dto.setOpeningTime(vendor.getOpeningTime());
        dto.setClosingTime(vendor.getClosingTime());
        dto.setEveningOpeningTime(vendor.getEveningOpeningTime());
        dto.setEveningClosingTime(vendor.getEveningClosingTime());
        dto.setAcceptingOrders(vendor.getAcceptingOrders());
        dto.setSlug(vendor.getSlug());
        dto.setCitySlug(vendor.getCitySlug());
        dto.setEtaMinutes(null);
        return dto;
    }

    public void updatePassword(String email, String newPassword) {
        Vendor v = vendorRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found"));
        v.setPassword(passwordEncoder.encode(newPassword));
        vendorRepository.save(v);
    }

    public int calculateETAForPublic(double distanceKm) {
        double avgSpeedKmH = 30.0;
        return (int) Math.ceil((distanceKm / avgSpeedKmH) * 60);
    }

    public List<Vendor> getVendorsByIds(List<Long> ids) {
        return vendorRepository.findAllById(ids);
    }

    // =========================
    // Availability toggle
    // =========================
    public VendorResponseDTO updateAvailability(Long vendorId, boolean acceptingOrders) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found"));
        vendor.setAcceptingOrders(acceptingOrders);
        Vendor saved = vendorRepository.save(vendor);
        return toResponseDTO(saved);
    }

    public List<Vendor> searchRestaurants(String query, int limit) {
        String q = (query == null) ? "" : query.trim();
        return vendorRepository.searchVendorsAnyType(q, PageRequest.of(0, limit));
    }
}
