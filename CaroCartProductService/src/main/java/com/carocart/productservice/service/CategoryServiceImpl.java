package com.carocart.productservice.service;

import com.carocart.productservice.dto.SubCategoryDTO;
import com.carocart.productservice.dto.AdminResponseDTO;
import com.carocart.productservice.dto.VendorResponseDTO;
import com.carocart.productservice.entity.Category;
import com.carocart.productservice.entity.SubCategory;
import com.carocart.productservice.feign.AdminClient;
import com.carocart.productservice.feign.VendorClient;
import com.carocart.productservice.repository.CategoryRepository;
import com.carocart.productservice.repository.ProductRepository;
import com.carocart.productservice.repository.SubCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepo;

    @Autowired
    private SubCategoryRepository subCategoryRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private AdminClient adminClient;

    @Autowired
    private VendorClient vendorClient;

    private String toSlug(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }

    private String generateSlug(String name, boolean appendUUID) {
        String baseSlug = toSlug(name);
        return appendUUID
                ? baseSlug + "-" + UUID.randomUUID().toString().substring(0, 8)
                : baseSlug;
    }

    @Override
    public Category addCategory(Category category, String token) {
        validateAdmin(token);

        if (categoryRepo.findByNameIgnoreCase(category.getName()).isPresent()) {
            throw new IllegalArgumentException("Category name already exists");
        }

        if (category.getSlug() == null || category.getSlug().isEmpty()) {
            category.setSlug(generateSlug(category.getName(), false));
        }

        return categoryRepo.save(category);
    }

    @Override
    public SubCategory addSubCategory(SubCategory subCategory, String token) {
        validateAdmin(token);

        boolean duplicate = subCategoryRepo.findByCategoryId(subCategory.getCategory().getId())
                .stream()
                .anyMatch(sc -> sc.getName().equalsIgnoreCase(subCategory.getName()));
        if (duplicate) {
            throw new IllegalArgumentException("Subcategory name already exists in this category");
        }

        if (subCategory.getSlug() == null || subCategory.getSlug().isEmpty()) {
            subCategory.setSlug(generateSlug(subCategory.getName(), false));
        }

        return subCategoryRepo.save(subCategory);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }

    @Override
    public List<SubCategory> getSubCategoriesByCategoryId(Long categoryId) {
        return subCategoryRepo.findByCategoryId(categoryId);
    }

    @Override
    public SubCategory getSubCategoryById(Long id) {
        return subCategoryRepo.findById(id).orElse(null);
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepo.findById(id).orElse(null);
    }

    @Override
    public void deleteCategoryById(Long categoryId, String token) {
        validateAdmin(token);
        if (!categoryRepo.existsById(categoryId)) {
            throw new RuntimeException("Category not found with ID: " + categoryId);
        }
        categoryRepo.deleteById(categoryId);
    }

    @Override
    public void deleteSubCategoryById(Long subcategoryId, String token) {
        validateAdmin(token);
        if (!subCategoryRepo.existsById(subcategoryId)) {
            throw new RuntimeException("Subcategory not found with ID: " + subcategoryId);
        }
        subCategoryRepo.deleteById(subcategoryId);
    }

    @Override
    public Category saveCategory(Category category) {
        return categoryRepo.save(category);
    }

    @Override
    public Category saveCategory(Category category, String token) {
        validateAdmin(token);
        return categoryRepo.save(category);
    }

    @Override
    public Category getCategoryBySlug(String slug) {
        return categoryRepo.findBySlug(slug).orElse(null);
    }

    @Override
    public SubCategory getSubCategoryBySlug(String slug) {
        return subCategoryRepo.findBySlug(slug).orElse(null);
    }

    @Override
    public void generateMissingSlugsForCategories() {
        List<Category> all = categoryRepo.findAll();
        boolean updated = false;
        for (Category cat : all) {
            if (cat.getSlug() == null || cat.getSlug().isEmpty()) {
                cat.setSlug(generateSlug(cat.getName(), false));
                updated = true;
            }
        }
        if (updated) categoryRepo.saveAll(all);
    }

    @Override
    public void generateMissingSlugsForSubCategories() {
        List<SubCategory> all = subCategoryRepo.findAll();
        boolean updated = false;
        for (SubCategory sub : all) {
            if (sub.getSlug() == null || sub.getSlug().isEmpty()) {
                sub.setSlug(generateSlug(sub.getName(), false));
                updated = true;
            }
        }
        if (updated) subCategoryRepo.saveAll(all);
    }

    @Override
    public List<Category> getCategoriesByVendorId(Long vendorId) {
        return categoryRepo.findByVendorId(vendorId);
    }

    @Override
    public List<SubCategory> getSubCategoriesByVendorIds(List<Long> vendorIds) {
        return subCategoryRepo.findByVendorIds(vendorIds);
    }

    @Override
    public List<Long> getVendorIdsBySubCategory(Long subCategoryId) {
        return productRepo.findVendorIdsBySubCategoryId(subCategoryId);
    }

    @Override
    public List<VendorResponseDTO> getNearbyVendorsBySubCategory(Long subCategoryId, double lat, double lng, double radiusKm) {
        List<Long> vendorIds = getVendorIdsBySubCategory(subCategoryId);
        if (vendorIds.isEmpty()) return List.of();

        List<VendorResponseDTO> vendors = vendorClient.getVendorsByIds(vendorIds);

        return vendors.stream()
                .filter(v -> v.getLatitude() != null && v.getLongitude() != null)
                .filter(v -> haversineDistanceKm(lat, lng, v.getLatitude(), v.getLongitude()) <= radiusKm)
                .peek(v -> {
                    double dist = haversineDistanceKm(lat, lng, v.getLatitude(), v.getLongitude());
                    v.setEtaMinutes(calculateETAForPublic(dist));
                })
                .toList();
    }

    // ✅ Update subcategory (partial)
    @Override
    public SubCategory updateSubCategory(Long id, SubCategoryDTO dto, String token) {
        validateAdmin(token);
        SubCategory existing = subCategoryRepo.findById(id).orElse(null);
        if (existing == null) return null;

        if (dto.getName() != null && !dto.getName().isBlank()) {
            existing.setName(dto.getName());
            if (existing.getSlug() == null || existing.getSlug().isBlank()) {
                existing.setSlug(generateSlug(existing.getName(), false));
            }
        }
        if (dto.getImageUrl() != null && !dto.getImageUrl().isBlank()) {
            existing.setImageUrl(dto.getImageUrl());
        }
        if (dto.getCategoryId() != null &&
                (existing.getCategory() == null || !dto.getCategoryId().equals(existing.getCategory().getId()))) {
            Category newCat = categoryRepo.findById(dto.getCategoryId()).orElse(null);
            if (newCat != null) existing.setCategory(newCat);
        }
        return subCategoryRepo.save(existing);
    }

    private double haversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private int calculateETAForPublic(double distanceKm) {
        return (int) Math.ceil((distanceKm / 30) * 60);
    }

    // ✅ single, public validateAdmin used everywhere
    @Override
    public void validateAdmin(String token) {
        AdminResponseDTO admin = adminClient.getCurrentAdmin(token);
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("Unauthorized: Admin role required");
        }
    }

    // ✅ Implemented missing method
    @Override
    public SubCategory saveSubCategory(SubCategory subCategory, String token) {
        validateAdmin(token);
        return subCategoryRepo.save(subCategory);
    }
}
