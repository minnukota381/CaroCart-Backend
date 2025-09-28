package com.carocart.productservice.service;

import com.carocart.productservice.dto.*;
import com.carocart.productservice.entity.Category;
import com.carocart.productservice.entity.Product;
import com.carocart.productservice.entity.SubCategory;
import com.carocart.productservice.feign.AdminClient;
import com.carocart.productservice.feign.VendorClient;
import com.carocart.productservice.repository.CategoryRepository;
import com.carocart.productservice.repository.ProductRepository;
import com.carocart.productservice.repository.SubCategoryRepository;
import com.carocart.productservice.util.FirebaseStorageUtil;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.carocart.productservice.dto.ProductAdminRowDTO;
import com.carocart.productservice.entity.ProductType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final VendorClient vendorClient;
    private final AdminClient adminClient;
    private final FirebaseStorageUtil firebaseStorageUtil;

    @Autowired
    public ProductServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            SubCategoryRepository subCategoryRepository,
            VendorClient vendorClient,
            AdminClient adminClient,
            FirebaseStorageUtil firebaseStorageUtil
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.vendorClient = vendorClient;
        this.adminClient = adminClient;
        this.firebaseStorageUtil = firebaseStorageUtil;
    }

    private boolean isAdmin(AdminResponseDTO admin) {
        return admin != null && "ADMIN".equals(admin.getRole());
    }

    private boolean isVendor(VendorResponseDTO vendor) {
        return vendor != null && "VENDOR".equals(vendor.getRole());
    }

    private AdminResponseDTO tryGetAdmin(String token) {
        try { return adminClient.getCurrentAdmin(token); } catch (Exception e) { return null; }
    }

    private VendorResponseDTO tryGetVendor(String token) {
        try { return vendorClient.getCurrentVendor(token); } catch (Exception e) { return null; }
    }

    private String toSlug(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }

    @Override
    public Product addProduct(Product product, MultipartFile image, String token) throws IOException {
        AdminResponseDTO admin = tryGetAdmin(token);
        VendorResponseDTO vendor = tryGetVendor(token);

        if (isAdmin(admin)) {
            // Admin can add any product
            product.setAddedByRole("ADMIN");
            product.setVendorId(null);
            product.setVendorName(null);
            product.setVendorEmail(null);

        } else if (isVendor(vendor)) {
            // Vendor restrictions
            product.setAddedByRole("VENDOR");
            product.setVendorId(vendor.getId());
            product.setVendorName(vendor.getFirstName() + " " + vendor.getLastName());
            product.setVendorEmail(vendor.getEmail());

            if (product.getProductType() == null) {
                throw new IllegalArgumentException("Product type is required");
            }

            // VendorResponseDTO.vendorType is a String (e.g., "FOOD" / "GROCERY")
            String vendorType = vendor.getVendorType();
            if (vendorType == null || vendorType.isBlank()) {
                throw new IllegalArgumentException("Vendor type is missing on vendor profile");
            }

            // Compare ProductType enum name() to vendorType string
            if (!product.getProductType().name().equalsIgnoreCase(vendorType)) {
                throw new IllegalArgumentException(
                    "Vendor of type " + vendorType + " can only add products of type " + vendorType
                );
            }

        } else {
            throw new RuntimeException("Unauthorized: Only admin or vendor can add products");
        }

        if (product.getSubCategory() == null) {
            throw new RuntimeException("SubCategory must be provided");
        }

        product.setSlug(toSlug(product.getName()) + "-" + UUID.randomUUID().toString().substring(0, 8));

        if (image != null && !image.isEmpty()) {
            product.setImageUrl(firebaseStorageUtil.uploadFile(image));
        }

        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }


    @Override
    public Product updateProduct(Long id, Product updatedProduct, MultipartFile image, String token) throws IOException {
        AdminResponseDTO admin = tryGetAdmin(token);
        VendorResponseDTO vendor = tryGetVendor(token);

        return productRepository.findById(id).map(product -> {
            boolean isOwner = isAdmin(admin) ||
                    (isVendor(vendor) && "VENDOR".equals(product.getAddedByRole())
                            && vendor.getId().equals(product.getVendorId()));
            if (!isOwner) throw new RuntimeException("Unauthorized: Not allowed to update this product");

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
            product.setProductType(updatedProduct.getProductType());

            // Set food/grocery-specific fields
            product.setPackSize(updatedProduct.getPackSize());
            product.setManufacturer(updatedProduct.getManufacturer());
            product.setFssaiLicenseNo(updatedProduct.getFssaiLicenseNo());
            product.setCountryOfOrigin(updatedProduct.getCountryOfOrigin());
            product.setIngredients(updatedProduct.getIngredients());
            product.setNutritionInfo(updatedProduct.getNutritionInfo());
            product.setReturnPolicy(updatedProduct.getReturnPolicy());

            product.setFoodCategory(updatedProduct.getFoodCategory());
            product.setVeg(updatedProduct.getVeg());
            product.setRestaurantName(updatedProduct.getRestaurantName());
            product.setCustomizationOptions(updatedProduct.getCustomizationOptions());
            product.setAddOns(updatedProduct.getAddOns());
            product.setBestseller(updatedProduct.getBestseller());

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
        VendorResponseDTO vendor = tryGetVendor(token);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        boolean isOwner = isAdmin(admin) ||
                (isVendor(vendor) && "VENDOR".equals(product.getAddedByRole())
                        && vendor.getId().equals(product.getVendorId()));

        if (!isOwner) throw new RuntimeException("Unauthorized: Not allowed to delete this product");

        productRepository.deleteById(id);
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
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public void generateMissingSlugs() {
        List<Product> allProducts = productRepository.findAll();
        int updatedCount = 0;
        for (Product product : allProducts) {
            if (product.getSlug() == null || product.getSlug().isEmpty()) {
                product.setSlug(toSlug(product.getName()) + "-" + UUID.randomUUID().toString().substring(0, 8));
                updatedCount++;
            }
        }
        if (updatedCount > 0) productRepository.saveAll(allProducts);
    }

    @Override
    public ProductDTO getProductDTOById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return toProductDTO(product);
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
        dto.setVendorName(product.getVendorName());

        if (product.getVendorId() != null) {
            try {
                VendorResponseDTO vendor = vendorClient.getVendorById(product.getVendorId());
                dto.setVendorEmail(vendor.getEmail());
            } catch (Exception e) {
                dto.setVendorEmail(product.getVendorEmail());
            }
        }
        return dto;
    }

    @Override
    public List<Product> getProductsBySubCategory(Long subCategoryId) {
        return productRepository.findBySubCategoryId(subCategoryId);
    }

    @Override
    public List<Product> getProductsByVendor(String token) {
        VendorResponseDTO vendor = tryGetVendor(token);
        if (!isVendor(vendor)) throw new RuntimeException("Unauthorized: Only vendors can access this.");
        return productRepository.findByAddedByRoleAndVendorId("VENDOR", vendor.getId());
    }

    @Override
    public List<Product> getProductsFromNearbyVendors(String type, double lat, double lng, double radiusKm,
                                                       Long categoryId, Long subCategoryId) {
        List<VendorResponseDTO> vendors = vendorClient.getNearbyVendors(type, lat, lng, radiusKm);
        if (vendors == null || vendors.isEmpty()) return new ArrayList<>();

        List<Long> vendorIds = vendors.stream().map(VendorResponseDTO::getId).toList();

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
        Map<String, Map<String, List<Product>>> grouped = new HashMap<>();
        for (Product p : products) {
            String cat = (p.getSubCategory() != null && p.getSubCategory().getCategory() != null)
                    ? p.getSubCategory().getCategory().getName()
                    : "Uncategorized";
            String sub = (p.getSubCategory() != null) ? p.getSubCategory().getName() : "Uncategorized";
            grouped.computeIfAbsent(cat, k -> new HashMap<>())
                    .computeIfAbsent(sub, k -> new ArrayList<>())
                    .add(p);
        }
        return grouped;
    }

    @Override
    public List<Product> getProductsByCategoryId(Long categoryId) {
        return productRepository.findBySubCategory_Category_Id(categoryId);
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
                predicates.add(cb.and(cb.greaterThan(root.get("stock"), 0), cb.isTrue(root.get("isAvailable"))));
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
    public long countAllProducts() {
        return productRepository.count();
    }

    @Override
    public SearchResultDTO unifiedSearch(String query) {
        SearchResultDTO result = new SearchResultDTO();

        // --- Products ---
        List<Product> products = productRepository
                .findTop8ByNameIgnoreCaseContainingOrBrandIgnoreCaseContainingOrDescriptionIgnoreCaseContaining(
                        query, query, query);
        result.setProducts(products.stream().map(this::toProductDTO).toList());

        // --- Categories ---
        List<Category> categories = categoryRepository.findTop5ByNameIgnoreCaseContaining(query);
        result.setCategories(categories.stream().map(cat -> {
            CategoryResponseDTO dto = new CategoryResponseDTO();
            dto.setId(cat.getId());
            dto.setName(cat.getName());
            dto.setImageUrl(cat.getImageUrl());
            dto.setSlug(cat.getSlug());
            return dto;
        }).toList());

        // --- SubCategories ---
        List<SubCategory> subCats = subCategoryRepository.findTop5ByNameIgnoreCaseContaining(query);
        result.setSubCategories(subCats.stream().map(sub -> {
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
        }).toList());

        // --- Brands ---
        result.setBrands(products.stream()
                .map(Product::getBrand)
                .filter(b -> b != null && !b.isEmpty())
                .distinct()
                .limit(5)
                .toList());

        // --- Vendors (via Feign) ---
        try {
            List<VendorResponseDTO> vendors = vendorClient.searchRestaurants(query, 5);
            result.setVendors(vendors);
        } catch (Exception e) {
            result.setVendors(List.of()); // fallback
        }

        return result;
    }

    
    @Override
    public Page<ProductAdminRowDTO> searchProductsAdmin(
            String q, String vendorType, Long vendorId, Long categoryId, Long subCategoryId,
            String availability, String stockView, Double minPrice, Double maxPrice,
            int page, int size, String sortBy, String sortDir, int lowStockThreshold
    ) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (q != null && !q.trim().isEmpty()) {
                String likeQuery = "%" + q.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), likeQuery),
                    cb.like(cb.lower(root.get("brand")), likeQuery),
                    cb.like(cb.lower(root.get("description")), likeQuery)
                ));
            }

            if (vendorType != null && !"ALL".equalsIgnoreCase(vendorType)) {
                predicates.add(cb.equal(root.get("productType"), ProductType.valueOf(vendorType.toUpperCase())));
            }

            if (vendorId != null) {
                predicates.add(cb.equal(root.get("vendorId"), vendorId));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("subCategory").get("category").get("id"), categoryId));
            }

            if (subCategoryId != null) {
                predicates.add(cb.equal(root.get("subCategory").get("id"), subCategoryId));
            }

            if ("YES".equalsIgnoreCase(availability)) {
                predicates.add(cb.isTrue(root.get("isAvailable")));
            } else if ("NO".equalsIgnoreCase(availability)) {
                predicates.add(cb.isFalse(root.get("isAvailable")));
            }

            if ("LOW".equalsIgnoreCase(stockView)) {
                predicates.add(cb.and(
                    cb.greaterThan(root.get("stock"), 0),
                    cb.lessThan(root.get("stock"), lowStockThreshold)
                ));
            } else if ("OUT".equalsIgnoreCase(stockView)) {
                predicates.add(cb.equal(root.get("stock"), 0));
            }

            if (minPrice != null) {
                predicates.add(cb.ge(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.le(root.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        return productPage.map(p -> {
            ProductAdminRowDTO dto = new ProductAdminRowDTO();
            dto.setId(p.getId());
            dto.setName(p.getName());
            dto.setImageUrl(p.getImageUrl());
            dto.setPrice(p.getPrice());
            dto.setMrp(p.getMrp());
            dto.setDiscount(p.getDiscount());
            dto.setStock(p.getStock());
            dto.setIsAvailable(p.getIsAvailable());
            dto.setVendorId(p.getVendorId());
            dto.setVendorName(p.getVendorName());
            dto.setVendorType(p.getProductType() != null ? p.getProductType().name() : null);
            dto.setCategoryName(
                p.getSubCategory() != null && p.getSubCategory().getCategory() != null
                    ? p.getSubCategory().getCategory().getName()
                    : null
            );
            dto.setSubCategoryName(
                p.getSubCategory() != null ? p.getSubCategory().getName() : null
            );
            dto.setUpdatedAt(p.getUpdatedAt());
            return dto;
        });
    }

    @Override
    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }
    
    
    @Override
    public long countProductsForCurrentVendor(String token) {
        VendorResponseDTO vendor = tryGetVendor(token);
        if (!isVendor(vendor)) {
            throw new RuntimeException("Unauthorized: Only vendors can access this.");
        }
        return productRepository.countByAddedByRoleAndVendorId("VENDOR", vendor.getId());
    }
}
