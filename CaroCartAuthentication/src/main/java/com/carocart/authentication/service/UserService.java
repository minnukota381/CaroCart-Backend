package com.carocart.authentication.service;

import com.carocart.authentication.config.DuplicateResourceException;
import com.carocart.authentication.dto.EmailRequest;
import com.carocart.authentication.dto.UserRegistrationDTO;
import com.carocart.authentication.entity.User;
import com.carocart.authentication.feign.NotificationClient;
import com.carocart.authentication.repository.UserRepository;
import com.carocart.authentication.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private NotificationClient notificationClient;

    @Transactional
    public void saveAndFlush(User user) {
        userRepository.saveAndFlush(user);
    }

    // Minimal helper: normalize to +91XXXXXXXXXX (keeps uniqueness robust)
    private String normalizePhone(String raw) {
        if (raw == null) return null;
        String digits = raw.trim().replaceAll("[\\s\\-()]", "");
        if (digits.matches("\\+\\d{8,15}")) return digits;
        if (digits.matches("\\d{10}")) return "+91" + digits;
        return digits; // DTO validation should prevent odd values
    }

    public String signup(UserRegistrationDTO dto) {
        // Email required + unique
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("email", "Email already registered");
        }

        // Phone required + unique (normalized)
        String phoneNorm = normalizePhone(dto.getPhoneNumber());
        if (userRepository.existsByPhoneNumber(phoneNorm)) {
            throw new DuplicateResourceException("phoneNumber", "Phone number already registered");
        }

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhoneNumber(phoneNorm);
        user.setDob(dto.getDob());
        user.setRole("USER");

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // In case of race → handled by GlobalExceptionHandler to 409
            throw e;
        }

        // Best-effort welcome email
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setToEmail(user.getEmail());
        emailRequest.setSubject("Welcome to CaroCart!");
        emailRequest.setBody("Hi " + user.getFirstName() + ",\n\nThanks for registering at CaroCart.\nEnjoy shopping with us!");
        try { notificationClient.sendEmail(emailRequest); } catch (Exception ignored) {}

        return "User registered successfully";
    }

    public boolean updateProfileImageUrl(String email, String imageUrl) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setProfileImageUrl(imageUrl);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public String login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) throw new IllegalArgumentException("Invalid email or password");

        User user = userOptional.get();
        if (user.getPassword() == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean updateUserProfile(String email, User updatedUser) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setDob(updatedUser.getDob());

        // Enforce phone uniqueness on update too (minimal)
        if (updatedUser.getPhoneNumber() != null && !updatedUser.getPhoneNumber().isBlank()) {
            String phoneNorm = normalizePhone(updatedUser.getPhoneNumber());
            Optional<User> clash = userRepository.findByPhoneNumber(phoneNorm);
            if (clash.isPresent() && !clash.get().getId().equals(user.getId())) {
                throw new DuplicateResourceException("phoneNumber", "Phone number already registered");
            }
            user.setPhoneNumber(phoneNorm);
        }

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw e;
        }
        return true;
    }

    public boolean updatePassword(String email, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public boolean changePassword(String email, String currentPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword() == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
                return false;
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public List<User> getAllUsers() { return userRepository.findAll(); }
    public void save(User user) { userRepository.save(user); }
}
