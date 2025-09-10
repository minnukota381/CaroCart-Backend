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

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth/phone")
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

    /**
     * Frontend sends Firebase ID token after successful phone OTP.
     * Response: { token, userId, phone, newUser }
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> body) {
        String idToken = body.get("idToken");
        if (idToken == null || idToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "idToken required"));
        }

        final FirebaseToken decoded;
        try {
            // If you want to reject revoked tokens, use verifyIdToken(idToken, true)
            decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "invalid_id_token"));
        }

        String phone = Optional.ofNullable(decoded.getClaims().get("phone_number"))
                               .map(Object::toString)
                               .orElse(null);
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "token_has_no_phone"));
        }

        boolean newUser = false;

        // 1) Find by phone
        User user = userRepository.findByPhoneNumber(phone).orElse(null);

        // 2) If not found, create a minimal user (phone-first)
        if (user == null) {
            user = new User();
            user.setPhoneNumber(phone);                 // REQUIRED + UNIQUE in your entity
            user.setFirstName("CaroCart");              // optional default
            user.setLastName("");
            user.setRole("USER");
            // Random password (unused for phone-only accounts, keep nullable if you made it optional)
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user = userRepository.save(user);
            newUser = true;
        }

        // (Optional) Keep any updates in sync (e.g., if you later collect email/name)
        // userRepository.save(user);

        // 3) Issue your own app JWT
        String jwt = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),    // can be null
                user.getRole(),
                user.getFirstName(),
                user.getLastName()
        );

        return ResponseEntity.ok(Map.of(
                "token", jwt,
                "userId", user.getId(),
                "phone", phone,
                "newUser", newUser,
                "issuedAt", Instant.now().toString()
        ));
    }
}
