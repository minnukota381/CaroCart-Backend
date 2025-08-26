package com.carocart.delivery.service.impl;

import com.carocart.delivery.dto.*;
import com.carocart.delivery.entity.DeliveryBoy;
import com.carocart.delivery.feign.NotificationClient;
import com.carocart.delivery.repository.DeliveryBoyRepository;
import com.carocart.delivery.service.DeliveryBoyOtpEmailService;
import com.carocart.delivery.service.DeliveryBoyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DeliveryBoyServiceImpl implements DeliveryBoyService {

    @Autowired
    private DeliveryBoyOtpEmailService otpEmailService;

    @Autowired
    private DeliveryBoyRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificationClient notificationClient;

    @Override
    public DeliveryBoyDTO register(DeliveryBoyRegistrationDTO dto) {
        try {
            if (repo.existsByPhone(dto.getPhone()))
                throw new RuntimeException("Phone already registered");
            if (dto.getEmail() != null && repo.existsByEmail(dto.getEmail()))
                throw new RuntimeException("Email already registered");

            DeliveryBoy boy = new DeliveryBoy();

            boy.setFirstName(dto.getFirstName());
            boy.setLastName(dto.getLastName());
            boy.setEmail(dto.getEmail());
            boy.setPhone(dto.getPhone());
            boy.setAddress(dto.getAddress());
            boy.setCity(dto.getCity());

            boy.setAadhaarFrontUrl(dto.getAadhaarFrontUrl());
            boy.setAadhaarBackUrl(dto.getAadhaarBackUrl());
            boy.setPanUrl(dto.getPanUrl()); // change from getPanCardUrl()

            boy.setVehicleType(dto.getVehicleType());
            boy.setVehicleRegNo(dto.getVehicleNumber());
            boy.setLicenseUrl(dto.getLicenseUrl());
            boy.setRcUrl(dto.getRcBookUrl());

            boy.setAccountHolderName(dto.getAccountHolderName());
            boy.setAccountNumber(dto.getAccountNumber());
            boy.setIfscCode(dto.getIfscCode());
            boy.setBankName(dto.getBankName());

            boy.setLanguages(dto.getLanguages());
            boy.setWhatsapp(dto.getWhatsapp());
            boy.setDateOfBirth(LocalDate.parse(dto.getDateOfBirth())); // Ensure frontend sends date in YYYY-MM-DD
            boy.setDrivingLicenseNumber(dto.getDrivingLicenseNumber());
            boy.setPassbookUrl(dto.getPassbookUrl());
            boy.setUpiId(dto.getUpiId());
            boy.setProfilePhotoUrl(dto.getProfilePhotoUrl());

            boy.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
            boy.setStatus("PENDING");
            boy.setActive(true);

            repo.save(boy);

            return toDTO(boy);
        } catch (Exception e) {
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }

    @Override
    public DeliveryBoyDTO login(DeliveryBoyLoginDTO loginDTO) {
        Optional<DeliveryBoy> opt = repo.findByPhone(loginDTO.getPhoneOrEmail());
        DeliveryBoy user = opt.orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPasswordHash()))
            throw new RuntimeException("Invalid credentials");

        if (!"APPROVED".equalsIgnoreCase(user.getStatus()) && !"AVAILABLE".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("Delivery boy is not approved yet");
        }

        return toDTO(user);
    }

    @Override
    public DeliveryBoyDTO getById(Long id) {
        return repo.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public List<DeliveryBoyDTO> getAvailableDeliveryBoys() {
        return repo.findByStatus("AVAILABLE")
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<DeliveryBoyDTO> getDeliveryBoysByStatus(String status) {
        return repo.findByStatus(status)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public void updateDeliveryBoyStatus(Long id, String status) {
        DeliveryBoy boy = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery boy not found"));
        boy.setStatus(status);
        repo.save(boy);

        // Notify on approval
        if ("AVAILABLE".equalsIgnoreCase(status) || "APPROVED".equalsIgnoreCase(status)) {
            String subject = "Your CaroCart Delivery Account is Approved!";
            String body = "Hello " + (boy.getFirstName() != null ? boy.getFirstName() : boy.getPhone()) + ",\n\n"
                    + "Congratulations! Your delivery boy account has been approved. You can now start accepting delivery orders.\n\n"
                    + "Thank you for being with CaroCart.\n\nBest regards,\nCaroCart Team";

            try {
                notificationClient.sendEmail(new EmailRequest(boy.getEmail(), subject, body));
            } catch (Exception e) {
                System.err.println("Failed to send email notification: " + e.getMessage());
            }
        }
    }

    @Override
    public ResponseEntity<String> sendForgotPasswordOtp(String phoneOrEmail) {
        DeliveryBoy boy = phoneOrEmail.contains("@") ? findByEmail(phoneOrEmail) : findByPhone(phoneOrEmail);
        if (boy == null || boy.getEmail() == null) {
            return ResponseEntity.badRequest().body("No registered email found for this delivery boy");
        }

        String otp = otpEmailService.generateOtp();
        otpEmailService.storeOtp(boy.getEmail(), otp);
        otpEmailService.sendOtpEmail(boy.getEmail(), otp);
        return ResponseEntity.ok("OTP sent to registered email");
    }

    @Override
    public ResponseEntity<String> verifyForgotPasswordOtp(String phoneOrEmail, String otp) {
        DeliveryBoy boy = phoneOrEmail.contains("@") ? findByEmail(phoneOrEmail) : findByPhone(phoneOrEmail);
        if (boy == null || boy.getEmail() == null) {
            return ResponseEntity.badRequest().body("No registered email found for this delivery boy");
        }

        boolean isValid = otpEmailService.validateOtp(boy.getEmail(), otp);
        if (isValid) {
            otpEmailService.removeOtp(boy.getEmail());
            return ResponseEntity.ok("OTP verified");
        } else {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }
    }

    @Override
    public ResponseEntity<String> resetForgottenPassword(String phoneOrEmail, String password) {
        DeliveryBoy boy = phoneOrEmail.contains("@") ? findByEmail(phoneOrEmail) : findByPhone(phoneOrEmail);
        if (boy == null || boy.getEmail() == null) {
            return ResponseEntity.badRequest().body("No registered email found for this delivery boy");
        }

        boy.setPasswordHash(passwordEncoder.encode(password));
        repo.save(boy);
        return ResponseEntity.ok("Password updated successfully");
    }

    @Override
    public DeliveryBoy findByEmail(String email) {
        return repo.findByEmail(email).orElse(null);
    }

    @Override
    public DeliveryBoy findByPhone(String phone) {
        return repo.findByPhone(phone).orElse(null);
    }

    private DeliveryBoyDTO toDTO(DeliveryBoy entity) {
        DeliveryBoyDTO dto = new DeliveryBoyDTO();
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setDateOfBirth(entity.getDateOfBirth() != null ? entity.getDateOfBirth().toString() : null);
        dto.setPhone(entity.getPhone());
        dto.setWhatsapp(entity.getWhatsapp());
        dto.setCity(entity.getCity());
        dto.setAddress(entity.getAddress());
        dto.setLanguages(entity.getLanguages());
        dto.setProfilePhotoUrl(entity.getProfilePhotoUrl());
        dto.setAadhaarFrontUrl(entity.getAadhaarFrontUrl());
        dto.setAadhaarBackUrl(entity.getAadhaarBackUrl());
        dto.setPanUrl(entity.getPanUrl());
        dto.setLicenseUrl(entity.getLicenseUrl());
        dto.setRcUrl(entity.getRcUrl());
        dto.setPassbookUrl(entity.getPassbookUrl());
        dto.setVehicleType(entity.getVehicleType());
        dto.setVehicleRegNo(entity.getVehicleRegNo());
        dto.setDrivingLicenseNumber(entity.getDrivingLicenseNumber());
        dto.setAccountHolderName(entity.getAccountHolderName());
        dto.setAccountNumber(entity.getAccountNumber());
        dto.setBankName(entity.getBankName());
        dto.setIfscCode(entity.getIfscCode());
        dto.setUpiId(entity.getUpiId());
        dto.setEmail(entity.getEmail());
        dto.setStatus(entity.getStatus());
        dto.setCurrentLat(entity.getCurrentLat());
        dto.setCurrentLng(entity.getCurrentLng());

        return dto;
    }
    
    @Override
    public void updateCurrentLocation(Long deliveryBoyId, Double lat, Double lng) {
        DeliveryBoy boy = repo.findById(deliveryBoyId)
            .orElseThrow(() -> new RuntimeException("Not found"));
        boy.setCurrentLat(lat);
        boy.setCurrentLng(lng);
        repo.save(boy);
    }

    @Override
    public List<DeliveryBoyDTO> getAll() {
        // however you map entities → DTOs in your project
        return repo.findAll().stream()
            .map(this::toDTO)
            .toList();
    }


}
