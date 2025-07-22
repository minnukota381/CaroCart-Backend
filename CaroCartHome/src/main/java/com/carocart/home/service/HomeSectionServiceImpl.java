package com.carocart.home.service;

import com.carocart.home.entity.HomeSection;
import com.carocart.home.repository.HomeSectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HomeSectionServiceImpl implements HomeSectionService {
    @Autowired
    private HomeSectionRepository repo;

    @Override
    public List<HomeSection> getAllSections() {
        return repo.findAllByOrderBySectionOrderAsc();
    }
    @Override
    public HomeSection addSection(HomeSection section) { return repo.save(section); }
    @Override
    public HomeSection updateSection(Long id, HomeSection section) {
        section.setId(id);
        return repo.save(section);
    }
    @Override
    public void deleteSection(Long id) { repo.deleteById(id); }
}
