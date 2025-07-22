package com.carocart.home.controller;

import com.carocart.home.entity.Banner;
import com.carocart.home.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    // Create/Update Banner (admin uploads Firebase URL)
    @PostMapping
    public Banner addBanner(@RequestBody Banner banner) {
        return bannerService.saveBanner(banner);
    }

    // Get all banners (for frontend homepage)
    @GetMapping
    public List<Banner> getAllBanners() {
        return bannerService.getAllBanners();
    }

    // Update banner by ID
    @PutMapping("/{id}")
    public Banner updateBanner(@PathVariable Long id, @RequestBody Banner banner) {
        return bannerService.updateBanner(id, banner);
    }

    // Delete banner
    @DeleteMapping("/{id}")
    public void deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
    }
}
