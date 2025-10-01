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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


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
    public Product updateProduct(Long id, Product product, String token) throws IOException {
        Product existing = productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));

        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setMrp(product.getMrp());
        existing.setSubCategory(product.getSubCategory());
        existing.setImageUrl(product.getImageUrl()); // Firebase URL
        existing.setUnit(product.getUnit());
        existing.setStock(product.getStock());
        existing.setBrand(product.getBrand());
        existing.setFoodCategory(product.getFoodCategory());
        existing.setVeg(product.getVeg());
        existing.setRestaurantName(product.getRestaurantName());
        existing.setCustomizationOptions(product.getCustomizationOptions());
        existing.setAddOns(product.getAddOns());
        existing.setBestseller(product.getBestseller());
        existing.setPackSize(product.getPackSize());
        existing.setManufacturer(product.getManufacturer());
        existing.setFssaiLicenseNo(product.getFssaiLicenseNo());
        existing.setCountryOfOrigin(product.getCountryOfOrigin());
        existing.setIngredients(product.getIngredients());
        existing.setNutritionInfo(product.getNutritionInfo());
        existing.setReturnPolicy(product.getReturnPolicy());
        // update other fields as needed

        return productRepository.save(existing);
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
    public Page<Product> getAllProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAll(pageable);
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
    public Page<Product> getProductsBySubCategory(Long subCategoryId, int page, int size, String sortBy, String sortDir) {
        Sort sort = "asc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findBySubCategoryId(subCategoryId, pageable);
    }


    @Override
    public Page<Product> getProductsByVendor(String token, int page, int size, String sortBy, String sortDir) {
        VendorResponseDTO vendor = tryGetVendor(token);
        if (!isVendor(vendor)) throw new RuntimeException("Unauthorized: Only vendors can access this.");

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return productRepository.findByAddedByRoleAndVendorId("VENDOR", vendor.getId(), pageable);
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
    public Page<Product> getProductsByCategoryId(Long categoryId, int page, int size, String sortBy, String sortDir) {
        Sort sort = "asc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findBySubCategory_Category_Id(categoryId, pageable);
    }

    @Override
    public Page<Product> searchAndFilterProducts(String keyword, Long categoryId, Long subCategoryId,
                                                int page, int size, String sortBy, String sortDir) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isEmpty()) {
                String likeKeyword = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), likeKeyword),
                    cb.like(cb.lower(root.get("description")), likeKeyword),
                    cb.like(cb.lower(root.get("brand")), likeKeyword)
                ));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("subCategory").get("category").get("id"), categoryId));
            }
            if (subCategoryId != null) {
                predicates.add(cb.equal(root.get("subCategory").get("id"), subCategoryId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAll(spec, pageable);
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
    
    
    @Override
    public Map<String, Object> bulkUploadCsv(MultipartFile file, String token) throws IOException {
        AdminResponseDTO admin = tryGetAdmin(token);
        VendorResponseDTO vendor = tryGetVendor(token);

        boolean asAdmin = isAdmin(admin);
        boolean asVendor = isVendor(vendor);

        if (!asAdmin && !asVendor) {
            throw new RuntimeException("Unauthorized: Only admin or vendor can bulk upload");
        }

        // If vendor, lock productType and vendor fields
        String lockedVendorType = null; // "FOOD" or "GROCERY"
        if (asVendor) {
            if (vendor.getVendorType() == null || vendor.getVendorType().isBlank()) {
                throw new IllegalArgumentException("Vendor type is missing on vendor profile");
            }
            lockedVendorType = vendor.getVendorType().toUpperCase();
        }

        int total = 0;
        int success = 0;
        List<Map<String, Object>> errors = new ArrayList<>();

        try (CSVParser parser = CSVParser.parse(
                file.getInputStream(),
                java.nio.charset.StandardCharsets.UTF_8,
                CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .withTrim()
                        .withIgnoreEmptyLines(true))) {

            // Case-insensitive headers
            Map<String, Integer> headerMap = parser.getHeaderMap();
            // Required headers
            List<String> required = List.of("subCategoryId", "name", "price", "mrp");
            for (String r : required) {
                if (!headerMap.keySet().stream().anyMatch(h -> h.equalsIgnoreCase(r))) {
                    throw new IllegalArgumentException("Missing required header: " + r);
                }
            }

            for (CSVRecord row : parser) {
                total++;
                try {
                    // ---- Read fields (case-insensitive) ----
                    String subCategoryIdStr = get(row, "subCategoryId");
                    String name = get(row, "name");
                    String description = get(row, "description");
                    String priceStr = get(row, "price");
                    String mrpStr = get(row, "mrp");
                    String unit = get(row, "unit");
                    String imageUrl = get(row, "imageUrl");
                    String isAvailableStr = get(row, "isAvailable");
                    String stockStr = get(row, "stock");
                    String discountStr = get(row, "discount");
                    String brand = get(row, "brand");
                    String productTypeStr = get(row, "productType"); // optional for admin

                    // ---- Validate & parse ----
                    if (name == null || name.isBlank()) {
                        throw new IllegalArgumentException("name is required");
                    }
                    Long subCategoryId = parseLong(subCategoryIdStr, "subCategoryId");
                    Double price = parseDouble(priceStr, "price");
                    Double mrp = parseDouble(mrpStr, "mrp");
                    if (price > mrp) {
                        throw new IllegalArgumentException("Price must not exceed MRP");
                    }

                    SubCategory subCategory = subCategoryRepository.findById(subCategoryId)
                            .orElseThrow(() -> new IllegalArgumentException("Subcategory not found: " + subCategoryId));

                    // Determine product type
                    ProductType productType = null;
                    if (asVendor) {
                        productType = ProductType.valueOf(lockedVendorType); // lock to vendor type
                    } else if (productTypeStr != null && !productTypeStr.isBlank()) {
                        productType = ProductType.valueOf(productTypeStr.trim().toUpperCase());
                    } else {
                        // Optional: try to infer from subcategory/category naming—keeping simple:
                        throw new IllegalArgumentException("productType required for admin rows (FOOD/GROCERY)");
                    }

                    Boolean isAvailable = parseBoolean(isAvailableStr, true); // default true
                    Integer stock = parseInt(stockStr, 0);
                    Double discount = parseDoubleOrNull(discountStr);

                    // ---- Build Product and reuse addProduct() ----
                    Product p = new Product();
                    p.setName(name.trim());
                    p.setDescription(nvl(description));
                    p.setBrand(nvl(brand));
                    p.setPrice(price);
                    p.setMrp(mrp);
                    p.setDiscount(discount != null ? discount : 0.0);
                    p.setStock(stock);
                    p.setUnit(nvl(unit));
                    p.setIsAvailable(isAvailable);
                    p.setSubCategory(subCategory);
                    p.setImageUrl(nvl(imageUrl));
                    p.setProductType(productType);
                    // food/grocery-specifics can remain null by default

                    // This method applies role checks, fills vendor fields, slug, timestamps, etc.
                    addProduct(p, null, token);

                    success++;
                } catch (Exception rowEx) {
                    Map<String, Object> err = new HashMap<>();
                    err.put("row", row.getRecordNumber()); // 1-based excluding header
                    err.put("error", rowEx.getMessage());
                    errors.add(err);
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows", total);
        result.put("success", success);
        result.put("failed", total - success);
        result.put("errors", errors);
        return result;
    }

    // ---- small helpers (add anywhere in ProductServiceImpl) ----
    private static String get(CSVRecord row, String key) {
        // case-insensitive
        for (String h : row.toMap().keySet()) {
            if (h.equalsIgnoreCase(key)) return row.get(h);
        }
        return null;
    }

    private static String nvl(String s) { return s == null ? "" : s; }

    private static Long parseLong(String s, String field) {
        try { return Long.parseLong(s.trim()); }
        catch (Exception e) { throw new IllegalArgumentException(field + " must be a number"); }
    }

    private static Integer parseInt(String s, int def) {
        if (s == null || s.isBlank()) return def;
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return def; }
    }

    private static Double parseDouble(String s, String field) {
        try { return Double.parseDouble(s.trim()); }
        catch (Exception e) { throw new IllegalArgumentException(field + " must be a number"); }
    }

    private static Double parseDoubleOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.parseDouble(s.trim()); }
        catch (Exception e) { return null; }
    }

    private static Boolean parseBoolean(String s, boolean defVal) {
        if (s == null || s.isBlank()) return defVal;
        String t = s.trim().toLowerCase();
        if (t.equals("true") || t.equals("1") || t.equals("yes")) return true;
        if (t.equals("false") || t.equals("0") || t.equals("no")) return false;
        return defVal;
    }

}
