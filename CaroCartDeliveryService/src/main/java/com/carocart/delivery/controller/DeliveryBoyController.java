package com.carocart.delivery.controller;

import com.carocart.delivery.dto.DeliveryBoyDTO;
import com.carocart.delivery.dto.DeliveryBoyLoginDTO;
import com.carocart.delivery.service.DeliveryBoyService;
import com.carocart.delivery.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/deliveryboys")
public class DeliveryBoyController {

    @Autowired
    private DeliveryBoyService service;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public DeliveryBoyDTO register(@RequestBody DeliveryBoyDTO dto, @RequestParam String password) {
        return service.register(dto, password);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody DeliveryBoyLoginDTO loginDTO) {
        DeliveryBoyDTO user = service.login(loginDTO);
        String token = jwtUtil.generateToken(user.getId(), "DELIVERY_BOY", user.getName());
        return ResponseEntity.ok(Map.of("token", token, "user", user));
    }

    @GetMapping("/{id}")
    public DeliveryBoyDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }
}
