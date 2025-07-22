package com.carocart.home.ServiceImpl;

import com.carocart.home.entity.Banner;
import com.carocart.home.repository.BannerRepository;
import com.carocart.home.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BannerServiceImpl implements BannerService {

    @Autowired
    private BannerRepository bannerRepository;

    @Override
    public Banner saveBanner(Banner banner) {
        // Optionally: Check for duplicate bannerId and update
        Optional<Banner> existing = bannerRepository.findByBannerId(banner.getBannerId());
        if (existing.isPresent()) {
            Banner b = existing.get();
            b.setImageUrl(banner.getImageUrl());
            b.setLinkTo(banner.getLinkTo());
            b.setAlt(banner.getAlt());
            b.setSectionOrder(banner.getSectionOrder());
            return bannerRepository.save(b);
        }
        return bannerRepository.save(banner);
    }

    @Override
    public List<Banner> getAllBanners() {
        return bannerRepository.findAll();
    }

    @Override
    public Banner updateBanner(Long id, Banner banner) {
        Banner existing = bannerRepository.findById(id).orElseThrow();
        existing.setBannerId(banner.getBannerId());
        existing.setImageUrl(banner.getImageUrl());
        existing.setLinkTo(banner.getLinkTo());
        existing.setAlt(banner.getAlt());
        existing.setSectionOrder(banner.getSectionOrder());
        return bannerRepository.save(existing);
    }

    @Override
    public void deleteBanner(Long id) {
        bannerRepository.deleteById(id);
    }
}
