package com.carocart.delivery.controller;

import com.carocart.delivery.dto.DeliveryBoyDTO;
import com.carocart.delivery.dto.DeliveryBoyLoginDTO;
import com.carocart.delivery.service.DeliveryBoyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deliveryboys")
public class DeliveryBoyController {

    @Autowired
    private DeliveryBoyService service;

    @PostMapping("/register")
    public DeliveryBoyDTO register(@RequestBody DeliveryBoyDTO dto, @RequestParam String password) {
        return service.register(dto, password);
    }

    @PostMapping("/login")
    public DeliveryBoyDTO login(@RequestBody DeliveryBoyLoginDTO loginDTO) {
        return service.login(loginDTO);
    }

    @GetMapping("/{id}")
    public DeliveryBoyDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }
}
