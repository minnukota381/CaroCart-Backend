package com.carocart.home.service;

import com.carocart.home.entity.HomeSection;
import java.util.List;

public interface HomeSectionService {
    List<HomeSection> getAllSections();
    HomeSection addSection(HomeSection section);
    HomeSection updateSection(Long id, HomeSection section);
    void deleteSection(Long id);
}
