package com.carocart.home.service;

import com.carocart.home.entity.Banner;

import java.util.List;

public interface BannerService {
    Banner saveBanner(Banner banner);
    List<Banner> getAllBanners();
    Banner updateBanner(Long id, Banner banner);
    void deleteBanner(Long id);
}
