package com.carocart.home.repository;

import com.carocart.home.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    Optional<Banner> findByBannerId(String bannerId);
}
