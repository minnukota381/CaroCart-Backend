package com.carocart.authentication.controller;

import com.carocart.authentication.entity.User;
import com.carocart.authentication.repository.UserRepository;
import com.carocart.authentication.util.JwtUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/users/phone")
public class PhoneAuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public PhoneAuthController(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // Client does OTP with Firebase JS → sends Firebase ID token here.
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> body) {
        String idToken = body.get("idToken");
        if (idToken == null || idToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "idToken required"));
        }

        final FirebaseToken decoded;
        try {
            decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("error", "invalid_id_token"));
        }

        // Firebase Admin SDK exposes phone for phone-auth as "phone_number" claim
        String phone = Optional.ofNullable(decoded.getClaims().get("phone_number"))
                               .map(Object::toString)
                               .orElse(null);

        if (phone == null || phone.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "token_has_no_phone"));
        }

        // Optional display name (often absent for phone auth)
        String displayName = Optional.ofNullable(decoded.getClaims().get("name"))
                                     .map(Object::toString)
                                     .orElse("CaroCart");

        // Satisfy NOT NULL + UNIQUE email constraint via synthetic email
        String syntheticEmail = "phone_" + phone.replace("+", "") + "@carocart.local";

        User user = userRepository.findByEmail(syntheticEmail).orElseGet(() -> {
            User u = new User();
            u.setEmail(syntheticEmail);
            u.setFirstName(displayName);
            u.setLastName("");
            u.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // random/unused
            u.setPhoneNumber(phone);
            u.setRole("USER");
            return userRepository.save(u);
        });

        // keep phone synced
        if (!phone.equals(user.getPhoneNumber())) {
            user.setPhoneNumber(phone);
            userRepository.save(user);
        }

        String jwt = jwtUtil.generateToken(
            user.getId(), user.getEmail(), user.getRole(),
            user.getFirstName(), user.getLastName()
        );

        return ResponseEntity.ok(Map.of("token", jwt));
    }
}
