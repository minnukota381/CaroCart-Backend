package com.carocart.authentication.service;

import com.carocart.authentication.dto.EmailRequest;
import com.carocart.authentication.dto.UserRegistrationDTO;
import com.carocart.authentication.entity.User;
import com.carocart.authentication.feign.NotificationClient;
import com.carocart.authentication.repository.UserRepository;
import com.carocart.authentication.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private NotificationClient notificationClient;

    // -- UPDATED SIGNUP WITH DTO & VALIDATION --
    public String signup(UserRegistrationDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            return "Email already registered";
        }
        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setDob(dto.getDob());
        user.setRole("USER");
        userRepository.save(user);

        // Send welcome email via Notification microservice
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setToEmail(user.getEmail());
        emailRequest.setSubject("Welcome to CaroCart!");
        emailRequest.setBody("Hi " + user.getFirstName() + ",\n\nThanks for registering at CaroCart.\nEnjoy shopping with us!");
        try {
            notificationClient.sendEmail(emailRequest);
        } catch (Exception e) {
            System.out.println("Error sending email: " + e.getMessage());
        }

        return "User registered successfully";
    }

    public String login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        User user = userOptional.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return jwtUtil.generateToken(
        		user.getId(),
                user.getEmail(),
                "USER",
                user.getFirstName(),
                user.getLastName()
        );
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean updateUserProfile(String email, User updatedUser) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            user.setDob(updatedUser.getDob());
            user.setPhoneNumber(updatedUser.getPhoneNumber());
            userRepository.save(user);
            return true;
        }
        return false;
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
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                return false;
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
