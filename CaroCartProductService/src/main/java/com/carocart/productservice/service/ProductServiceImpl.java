package com.carocart.productservice.service;

import com.carocart.productservice.dto.AdminResponseDTO;
import com.carocart.productservice.dto.ProductDTO;
import com.carocart.productservice.dto.VendorDTO;
import com.carocart.productservice.entity.Product;
import com.carocart.productservice.feign.AdminClient;
import com.carocart.productservice.feign.VendorClient;
import com.carocart.productservice.repository.ProductRepository;
import com.carocart.productservice.util.FirebaseStorageUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import com.carocart.productservice.util.FirebaseStorageUtil;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;


@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

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

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteProduct(Long id, String token) {
        AdminResponseDTO admin = tryGetAdmin(token);
        VendorDTO vendor = tryGetVendor(token);

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        boolean isOwner = isAdmin(admin)
            || (isVendor(vendor) && "VENDOR".equals(product.getAddedByRole())
            && vendor.getId().equals(product.getAddedById()));

        if (!isOwner) {
            throw new RuntimeException("Unauthorized: Not allowed to delete this product");
        }

        productRepository.deleteById(id);
    }
    
    private String toSlug(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }
    
    
    
    @Override
    public Product getProductBySlug(String slug) {
        return productRepository.findBySlug(slug).orElse(null);
    }
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
            product.setAddedById(admin.getId());
            product.setVendorName(null);
        } else if (isVendor(vendor)) {
            product.setAddedByRole("VENDOR");
            product.setAddedById(vendor.getId());
            product.setVendorName(vendor.getFullName());
        } else {
            throw new RuntimeException("Unauthorized: Only admin or vendor can add products");
        }

        if (product.getSubCategory() == null) {
            throw new RuntimeException("SubCategory must be provided");
        }

        // Generate SEO-friendly slug
        String slug = toSlug(product.getName()) + "-" + UUID.randomUUID().toString().substring(0, 8);
        product.setSlug(slug);

        // Firebase image upload
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
                && vendor.getId().equals(product.getAddedById()));

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

            // Upload new image if provided
            try {
                if (image != null && !image.isEmpty()) {
                    String imageUrl = firebaseStorageUtil.uploadFile(image);
                    product.setImageUrl(imageUrl);
                }
            } catch (IOException e) {
                throw new RuntimeException("Image upload failed", e);
            }

            return productRepository.save(product);
        }).orElse(null);
    }
    
 // In your Product Service: com.carocart.productservice.service.ProductServiceImpl

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

        // CRUCIAL ADDITION: Set the vendorId from the Product entity
        // The Product entity (com.carocart.productservice.entity.Product) must have getAddedById()
        // which represents the vendor's ID if AddedByRole is "VENDOR".
        if ("VENDOR".equals(product.getAddedByRole()) && product.getAddedById() != null) {
            dto.setVendorId(product.getAddedById()); // <--- SET VENDOR ID HERE!
            try {
                VendorDTO vendor = vendorClient.getVendorById(product.getAddedById());
                dto.setVendorEmail(vendor.getEmail());
            } catch (Exception e) {
                dto.setVendorEmail(null);
                // Log this exception: e.g., "Vendor not found for product {id} when fetching DTO"
            }
        } else if ("ADMIN".equals(product.getAddedByRole())) {
            // If an admin added it, there might not be a direct vendorId.
            // You might want a default or null, or throw an error if all products must have an associated vendor.
            // For your cart service's 'one vendor' logic, if an admin-added product is in the cart,
            // its vendorId would be null if not explicitly set. You might need a rule for this.
            // For now, setting it to null or omitting it might be okay if admin products don't adhere to the vendor rule.
            dto.setVendorId(null); // Or handle as per your business logic for admin-added products
            dto.setVendorEmail("admin@example.com"); // Example, if needed
        } else {
            dto.setVendorId(null); // Fallback for products with no clear vendor or admin
            dto.setVendorEmail(null);
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

        return productRepository.findByAddedByRoleAndAddedById("VENDOR", vendor.getId());
    }
    
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
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

    public long countAllProducts() {
        return productRepository.count();
    }

    @Override
    public List<Product> getProductsFromNearbyVendors(String type, double lat, double lng, double radiusKm, Long categoryId, Long subCategoryId) {
        // 1. Get nearby vendors from Auth service
        List<VendorDTO> vendors = vendorClient.getNearbyVendors(type, lat, lng, radiusKm);
        if (vendors == null || vendors.isEmpty()) return new ArrayList<>();

        List<Long> vendorIds = vendors.stream()
                                      .map(VendorDTO::getId)
                                      .toList();

        // 2. Fetch products from these vendors (and category/subcategory filter)
        if (subCategoryId != null) {
            return productRepository.findByAddedByRoleAndAddedByIdInAndSubCategory_Id("VENDOR", vendorIds, subCategoryId);
        } else if (categoryId != null) {
            return productRepository.findByAddedByRoleAndAddedByIdInAndSubCategory_Category_Id("VENDOR", vendorIds, categoryId);
        } else {
            return productRepository.findByAddedByRoleAndAddedByIdIn("VENDOR", vendorIds);
        }
    }
    
    @Override
    public Map<String, Map<String, List<Product>>> getProductsGroupedByCategoryAndSubCategory(Long vendorId) {
        List<Product> products = productRepository.findByAddedByRoleAndAddedById("VENDOR", vendorId);

        // Structure: {categoryName -> {subCategoryName -> [Product, ...]}}
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


}
