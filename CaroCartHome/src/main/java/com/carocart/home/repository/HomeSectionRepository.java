package com.carocart.home.repository;

import com.carocart.home.entity.HomeSection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HomeSectionRepository extends JpaRepository<HomeSection, Long> {
    List<HomeSection> findAllByOrderBySectionOrderAsc();
}
