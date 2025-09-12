package com.carocart.address.controller;

import org.springframework.web.bind.annotation.*;

import com.carocart.address.service.GoogleDistanceService;

@RestController
@RequestMapping("/location")
public class DistanceController {
    private final GoogleDistanceService svc;

    public DistanceController(GoogleDistanceService svc) {
        this.svc = svc;
    }

    @GetMapping("/driving-km")
    public Double drivingKm(@RequestParam double oLat,
                            @RequestParam double oLng,
                            @RequestParam double dLat,
                            @RequestParam double dLng) {
        return svc.drivingKm(oLat, oLng, dLat, dLng);
    }
}
