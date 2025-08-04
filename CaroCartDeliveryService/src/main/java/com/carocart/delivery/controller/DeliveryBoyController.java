package com.carocart.delivery.controller;

import com.carocart.delivery.dto.DeliveryBoyDTO;
import com.carocart.delivery.dto.DeliveryBoyLoginDTO;
import com.carocart.delivery.dto.DeliveryBoyRegistrationDTO;
import com.carocart.delivery.dto.LocationUpdateDTO;
import com.carocart.delivery.entity.DeliveryBoy;
import com.carocart.delivery.service.DeliveryBoyService;
import com.carocart.delivery.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/deliveryboys")
public class DeliveryBoyController {

    @Autowired
    private DeliveryBoyService service;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerDeliveryBoy(@RequestBody DeliveryBoyRegistrationDTO dto) {
        DeliveryBoyDTO response = service.register(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody DeliveryBoyLoginDTO loginDTO) {
        DeliveryBoyDTO user = service.login(loginDTO);
        String fullName =
            ((user.getFirstName() != null ? user.getFirstName() : "") + " " +
            (user.getLastName() != null ? user.getLastName() : "")).trim();
        if (fullName.isEmpty()) fullName = user.getPhone();
        String token = jwtUtil.generateToken(user.getId(), "DELIVERY_BOY", user.getEmail());
        return ResponseEntity.ok(Map.of("token", token, "user", user));
    }

    // 👇 NEW: Get current delivery boy profile using JWT
    @GetMapping("/me")
    public DeliveryBoyDTO getCurrentDeliveryBoy(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader != null && authHeader.startsWith("Bearer ")
            ? authHeader.substring(7)
            : authHeader;
        Long userId = jwtUtil.extractUserId(token);
        return service.getById(userId);
    }

    @GetMapping("/{id}")
    public DeliveryBoyDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/available")
    public List<DeliveryBoyDTO> getAvailableDeliveryBoys() {
        return service.getAvailableDeliveryBoys();
    }

    @GetMapping("/status/{status}")
    public List<DeliveryBoyDTO> getDeliveryBoysByStatus(@PathVariable String status) {
        return service.getDeliveryBoysByStatus(status.toUpperCase());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateDeliveryBoyStatus(@PathVariable Long id, @RequestParam String status) {
        service.updateDeliveryBoyStatus(id, status.toUpperCase());
        return ResponseEntity.ok("Status updated");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String phoneOrEmail = request.get("phoneOrEmail");
        return service.sendForgotPasswordOtp(phoneOrEmail);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody Map<String, String> request) {
        String phoneOrEmail = request.get("phoneOrEmail");
        String otp = request.get("otp");
        return service.verifyForgotPasswordOtp(phoneOrEmail, otp);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String phoneOrEmail = request.get("phoneOrEmail");
        String password = request.get("password");
        return service.resetForgottenPassword(phoneOrEmail, password);
    }
    @PutMapping("/me/location")
    public ResponseEntity<String> updateCurrentLocation(@RequestBody LocationUpdateDTO location, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
        }
        String email = jwtUtil.extractUsername(token.substring(7));
        DeliveryBoy boy = service.findByEmail(email);
        if (boy == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user");
        }

        service.updateCurrentLocation(boy.getId(), location.getLat(), location.getLng());
        return ResponseEntity.ok("Location updated");
    }


    @GetMapping("/{id}/location")
    public Map<String, Double> getDeliveryBoyLocation(@PathVariable Long id) {
        DeliveryBoyDTO dto = service.getById(id);
        return Map.of("lat", dto.getCurrentLat(), "lng", dto.getCurrentLng());
    }


}
