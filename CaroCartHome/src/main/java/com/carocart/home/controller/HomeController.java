package com.carocart.home.controller;

import com.carocart.home.dto.HomeSectionResponseDTO;
import com.carocart.home.entity.HomeSection;
import com.carocart.home.service.HomeSectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/home")
public class HomeController {
    @Autowired
    private HomeSectionService service;

    // User: Get homepage sections (frontend uses this)
    @GetMapping("/sections")
    public List<HomeSection> getSections() {
        return service.getAllSections();
    }

    // Admin: Add section
    @PostMapping("/sections")
    public HomeSection addSection(@RequestBody HomeSection section) {
        return service.addSection(section);
    }

    // Admin: Update section
    @PutMapping("/sections/{id}")
    public HomeSection updateSection(@PathVariable Long id, @RequestBody HomeSection section) {
        return service.updateSection(id, section);
    }

    // Admin: Delete section
    @DeleteMapping("/sections/{id}")
    public void deleteSection(@PathVariable Long id) {
        service.deleteSection(id);
    }
    
    @GetMapping("/sections/with-data")
    public List<HomeSectionResponseDTO> getSectionsWithData() {
        return service.getSectionsWithResolvedData();
    }

}
