package com.carocart.home.service;
//HomeSectionService.java

import java.util.List;

import com.carocart.home.dto.HomeSectionResponseDTO;
import com.carocart.home.entity.HomeSection;

public interface HomeSectionService {
    List<HomeSection> getAllSections();
    HomeSection addSection(HomeSection section);
    HomeSection updateSection(Long id, HomeSection section);
    void deleteSection(Long id);

    // 🔄 Change return type here
    List<HomeSectionResponseDTO> getSectionsWithResolvedData();
}
