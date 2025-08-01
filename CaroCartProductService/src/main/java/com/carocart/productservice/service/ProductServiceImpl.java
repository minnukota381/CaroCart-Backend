package com.carocart.productservice.service;

import com.carocart.productservice.dto.AdminResponseDTO;
import com.carocart.productservice.dto.CategoryResponseDTO;
import com.carocart.productservice.dto.ProductDTO;
import com.carocart.productservice.dto.SearchResultDTO;
import com.carocart.productservice.dto.SubCategoryResponseDTO;
import com.carocart.productservice.dto.VendorDTO;
import com.carocart.productservice.entity.Category;
import com.carocart.productservice.entity.Product;
import com.carocart.productservice.entity.SubCategory;
import com.carocart.productservice.feign.AdminClient;
import com.carocart.productservice.feign.VendorClient;
import com.carocart.productservice.repository.CategoryRepository;
import com.carocart.productservice.repository.ProductRepository;
import com.carocart.productservice.repository.SubCategoryRepository;
import com.carocart.productservice.util.FirebaseStorageUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;


    @Autowired
    private AdminClient adminClient;

    @Autowired
    private VendorClient vendorClient;

    @Autowired
    private FirebaseStorageUtil firebaseStorageUtil;
    
    

    private boolean isAdmin(AdminResponseDTO admin) {
        return admin != null && "ADMIN".equals(admin.getRole());
    }

    private boolean isVendor(VendorDTO vendor) {
        return vendor != null && "VENDOR".equals(vendor.getRole());
    }

    private AdminResponseDTO tryGetAdmin(String token) {
        try {
            return adminClient.getCurrentAdmin(token);
        } catch (Exception e) {
            return null;
        }
    }

    private VendorDTO tryGetVendor(String token) {
        try {
            return vendorClient.getCurrentVendor(token);
        } catch (Exception e) {
            return null;
        }
    }

    private void validateAdmin(String token) {
        AdminResponseDTO admin = adminClient.getCurrentAdmin(token);
        if (!isAdmin(admin)) {
            throw new RuntimeException("Unauthorized: Admin role required");
        }
    }

    private String toSlug(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public Product getProductBySlug(String slug) {
        return productRepository.findBySlug(slug).orElse(null);
    }

    @Override
    public void generateMissingSlugs() {
        List<Product> allProducts = productRepository.findAll();
        int updatedCount = 0;

        for (Product product : allProducts) {
            if (product.getSlug() == null || product.getSlug().isEmpty()) {
                String slug = toSlug(product.getName()) + "-" + UUID.randomUUID().toString().substring(0, 8);
                product.setSlug(slug);
                updatedCount++;
            }
        }

        if (updatedCount > 0) {
            productRepository.saveAll(allProducts);
            System.out.println("Updated slugs for " + updatedCount + " products.");
        } else {
            System.out.println("All products already have slugs.");
        }
    }

    @Override
    public Product addProduct(Product product, MultipartFile image, String token) throws IOException {
        AdminResponseDTO admin = tryGetAdmin(token);
        VendorDTO vendor = tryGetVendor(token);

        if (isAdmin(admin)) {
            product.setAddedByRole("ADMIN");
            product.setVendorId(null);
            product.setVendorName(null);
        } else if (isVendor(vendor)) {
            product.setAddedByRole("VENDOR");
            product.setVendorId(vendor.getId());
            product.setVendorName(vendor.getFullName());
        } else {
            throw new RuntimeException("Unauthorized: Only admin or vendor can add products");
        }

        if (product.getSubCategory() == null) {
            throw new RuntimeException("SubCategory must be provided");
        }

        String slug = toSlug(product.getName()) + "-" + UUID.randomUUID().toString().substring(0, 8);
        product.setSlug(slug);

        if (image != null && !image.isEmpty()) {
            String imageUrl = firebaseStorageUtil.uploadFile(image);
            product.setImageUrl(imageUrl);
        }

        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, Product updatedProduct, MultipartFile image, String token) throws IOException {
        AdminResponseDTO admin = tryGetAdmin(token);
        VendorDTO vendor = tryGetVendor(token);

        return productRepository.findById(id).map(product -> {
            boolean isOwner = isAdmin(admin)
                    || (isVendor(vendor) && "VENDOR".equals(product.getAddedByRole())
                    && vendor.getId().equals(product.getVendorId()));

            if (!isOwner) {
                throw new RuntimeException("Unauthorized: Not allowed to update this product");
            }

            product.setName(updatedProduct.getName());
            product.setDescription(updatedProduct.getDescription());
            product.setBrand(updatedProduct.getBrand());
            product.setPrice(updatedProduct.getPrice());
            product.setMrp(updatedProduct.getMrp());
            product.setDiscount(updatedProduct.getDiscount());
            product.setStock(updatedProduct.getStock());
            product.setUnit(updatedProduct.getUnit());
            product.setIsAvailable(updatedProduct.getIsAvailable());
            product.setUpdatedAt(LocalDateTime.now());
            product.setSubCategory(updatedProduct.getSubCategory());

            if (image != null && !image.isEmpty()) {
                try {
                    String imageUrl = firebaseStorageUtil.uploadFile(image);
                    product.setImageUrl(imageUrl);
                } catch (IOException e) {
                    throw new RuntimeException("Image upload failed", e);
                }
            }

            return productRepository.save(product);
        }).orElse(null);
    }

    @Override
    public void deleteProduct(Long id, String token) {
        AdminResponseDTO admin = tryGetAdmin(token);
        VendorDTO vendor = tryGetVendor(token);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        boolean isOwner = isAdmin(admin)
                || (isVendor(vendor) && "VENDOR".equals(product.getAddedByRole())
                && vendor.getId().equals(product.getVendorId()));

        if (!isOwner) {
            throw new RuntimeException("Unauthorized: Not allowed to delete this product");
        }

        productRepository.deleteById(id);
    }

    @Override
    public ProductDTO getProductDTOById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setStock(product.getStock());
        dto.setAvailable(Boolean.TRUE.equals(product.getIsAvailable()));
        dto.setSlug(product.getSlug());

        if ("VENDOR".equals(product.getAddedByRole()) && product.getVendorId() != null) {
            dto.setVendorId(product.getVendorId());
            try {
                VendorDTO vendor = vendorClient.getVendorById(product.getVendorId());
                dto.setVendorEmail(vendor.getEmail());
            } catch (Exception e) {
                dto.setVendorEmail(null);
            }
        } else {
            dto.setVendorId(null);
            dto.setVendorEmail("admin@example.com");
        }

        return dto;
    }

    @Override
    public List<Product> getProductsBySubCategory(Long subCategoryId) {
        return productRepository.findBySubCategoryId(subCategoryId);
    }

    @Override
    public List<Product> getProductsByVendor(String token) {
        VendorDTO vendor = tryGetVendor(token);
        if (!isVendor(vendor)) {
            throw new RuntimeException("Unauthorized: Only vendors can access this.");
        }
        return productRepository.findByAddedByRoleAndVendorId("VENDOR", vendor.getId());
    }

    @Override
    public List<Product> getProductsFromNearbyVendors(String type, double lat, double lng, double radiusKm, Long categoryId, Long subCategoryId) {
        List<VendorDTO> vendors = vendorClient.getNearbyVendors(type, lat, lng, radiusKm);
        if (vendors == null || vendors.isEmpty()) return new ArrayList<>();

        List<Long> vendorIds = vendors.stream().map(VendorDTO::getId).toList();

        if (subCategoryId != null) {
            return productRepository.findByAddedByRoleAndVendorIdInAndSubCategory_Id("VENDOR", vendorIds, subCategoryId);
        } else if (categoryId != null) {
            return productRepository.findByAddedByRoleAndVendorIdInAndSubCategory_Category_Id("VENDOR", vendorIds, categoryId);
        } else {
            return productRepository.findByAddedByRoleAndVendorIdIn("VENDOR", vendorIds);
        }
    }

    @Override
    public Map<String, Map<String, List<Product>>> getProductsGroupedByCategoryAndSubCategory(Long vendorId) {
        List<Product> products = productRepository.findByAddedByRoleAndVendorId("VENDOR", vendorId);

        Map<String, Map<String, List<Product>>> grouped = new java.util.HashMap<>();

        for (Product p : products) {
            String cat = p.getSubCategory() != null && p.getSubCategory().getCategory() != null
                    ? p.getSubCategory().getCategory().getName()
                    : "Uncategorized";
            String sub = p.getSubCategory() != null
                    ? p.getSubCategory().getName()
                    : "Uncategorized";

            grouped.computeIfAbsent(cat, k -> new java.util.HashMap<>())
                    .computeIfAbsent(sub, k -> new java.util.ArrayList<>())
                    .add(p);
        }
        return grouped;
    }

    @Override
    public List<Product> searchAndFilterProducts(String keyword, Long categoryId, Long subCategoryId,
                                                 Double minPrice, Double maxPrice, Boolean inStock,
                                                 Boolean discounted, String sortBy, String sortDir) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("subCategory").get("category").get("id"), categoryId));
            }
            if (subCategoryId != null) {
                predicates.add(cb.equal(root.get("subCategory").get("id"), subCategoryId));
            }
            if (minPrice != null) {
                predicates.add(cb.ge(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.le(root.get("price"), maxPrice));
            }
            if (Boolean.TRUE.equals(inStock)) {
                predicates.add(cb.and(
                        cb.greaterThan(root.get("stock"), 0),
                        cb.isTrue(root.get("isAvailable"))
                ));
            }
            if (Boolean.TRUE.equals(discounted)) {
                predicates.add(cb.gt(root.get("discount"), 0));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        return productRepository.findAll(spec, sort);
    }

    @Override
    public List<Product> getProductsByCategoryId(Long categoryId) {
        return productRepository.findBySubCategory_Category_Id(categoryId);
    }

    @Override
    public long countAllProducts() {
        return productRepository.count();
    }
    
    @Override
    public SearchResultDTO unifiedSearch(String query) {
        SearchResultDTO result = new SearchResultDTO();

        // 1. Products (top 8 by name, brand, or description)
        List<Product> products = productRepository
            .findTop8ByNameIgnoreCaseContainingOrBrandIgnoreCaseContainingOrDescriptionIgnoreCaseContaining(
                query, query, query);

        List<ProductDTO> productDTOs = products.stream()
            .map(this::toProductDTO)
            .toList();
        result.setProducts(productDTOs);

        // 2. Categories (top 5)
        List<Category> categories = categoryRepository.findTop5ByNameIgnoreCaseContaining(query);
        List<CategoryResponseDTO> catDTOs = categories.stream().map(cat -> {
            CategoryResponseDTO dto = new CategoryResponseDTO();
            dto.setId(cat.getId());
            dto.setName(cat.getName());
            dto.setImageUrl(cat.getImageUrl());
            dto.setSlug(cat.getSlug());
            return dto;
        }).toList();
        result.setCategories(catDTOs);

        // 3. SubCategories (top 5, with categoryName/categorySlug)
        List<SubCategory> subCats = subCategoryRepository.findTop5ByNameIgnoreCaseContaining(query);
        List<SubCategoryResponseDTO> subCatDTOs = subCats.stream().map(sub -> {
            SubCategoryResponseDTO dto = new SubCategoryResponseDTO();
            dto.setId(sub.getId());
            dto.setName(sub.getName());
            dto.setSlug(sub.getSlug());
            if (sub.getCategory() != null) {
                dto.setCategoryId(sub.getCategory().getId());
                dto.setCategoryName(sub.getCategory().getName());
                dto.setCategorySlug(sub.getCategory().getSlug());
            }
            return dto;
        }).toList();
        result.setSubCategories(subCatDTOs);

        // 4. Brands (from products)
        List<String> brands = products.stream()
            .map(Product::getBrand)
            .filter(b -> b != null && !b.isEmpty())
            .distinct()
            .limit(5)
            .toList();
        result.setBrands(brands);

        return result;
    }
    
    private ProductDTO toProductDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setStock(product.getStock());
        dto.setAvailable(Boolean.TRUE.equals(product.getIsAvailable()));
        dto.setSlug(product.getSlug());
        dto.setVendorId(product.getVendorId());
        // Optionally set vendorEmail if you want
        return dto;
    }

}
