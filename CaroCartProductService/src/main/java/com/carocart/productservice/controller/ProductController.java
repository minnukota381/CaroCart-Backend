package com.carocart.productservice.controller;

import com.carocart.productservice.dto.ProductAdminRowDTO;
import com.carocart.productservice.dto.ProductDTO;
import com.carocart.productservice.dto.ProductRequestDTO;
import com.carocart.productservice.dto.SearchResultDTO;
import com.carocart.productservice.entity.Product;
import com.carocart.productservice.entity.SubCategory;
import com.carocart.productservice.repository.SubCategoryRepository;
import com.carocart.productservice.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final SubCategoryRepository subCategoryRepository;

    public ProductController(ProductService productService, SubCategoryRepository subCategoryRepository) {
        this.productService = productService;
        this.subCategoryRepository = subCategoryRepository;
    }

    // --- Create Product ---
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Product> addProduct(
        @RequestBody ProductRequestDTO productDTO,
        @RequestHeader("Authorization") String token
    ) throws IOException {
        if (productDTO.getPrice() > productDTO.getMrp()) {
            throw new IllegalArgumentException("Price must not exceed MRP");
        }

        SubCategory subCategory = subCategoryRepository.findById(productDTO.getSubCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Subcategory not found"));

        Product product = mapDtoToProduct(productDTO, subCategory);
        return ResponseEntity.ok(productService.addProduct(product, null, token));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequestDTO productDTO,
            @RequestHeader("Authorization") String token
    ) throws IOException {
        if (productDTO.getPrice() > productDTO.getMrp()) {
            return ResponseEntity.badRequest().body("Price must not exceed MRP");
        }

        SubCategory subCategory = subCategoryRepository.findById(productDTO.getSubCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Subcategory not found"));

        Product updatedProduct = mapDtoToProduct(productDTO, subCategory);
        Product updated = productService.updateProduct(id, updatedProduct, token);
        return ResponseEntity.ok(updated);
    }

    // --- Delete Product ---
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id,
                                                @RequestHeader("Authorization") String token) {
        productService.deleteProduct(id, token);
        return ResponseEntity.ok("Product deleted successfully");
    }

    // --- Get Product by ID ---
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(product);
    }

    // --- Get Product DTO by ID ---
    @GetMapping("/dto/{id}")
    public ResponseEntity<ProductDTO> getProductDTOById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductDTOById(id));
    }

    // --- Get All Products (paginated) ---
    @GetMapping("/all")
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Page<Product> productsPage = productService.getAllProducts(page, size, sortBy, sortDir);
        return ResponseEntity.ok(productsPage);
    }

    // --- Vendor's Products (paginated) ---
    @GetMapping("/vendor/my-products")
    public ResponseEntity<Page<Product>> getVendorProducts(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Page<Product> productsPage = productService.getProductsByVendor(token, page, size, sortBy, sortDir);
        return ResponseEntity.ok(productsPage);
    }

    // --- Vendor Grouped by Category/SubCategory ---
    @GetMapping("/vendor/{vendorId}/grouped")
    public ResponseEntity<?> getVendorProductsGroupedByCategoryAndSubCategory(@PathVariable Long vendorId) {
        return ResponseEntity.ok(productService.getProductsGroupedByCategoryAndSubCategory(vendorId));
    }

    // --- Products by SubCategory (paginated) ---
    @GetMapping("/subcategory/{subCategoryId}")
    public ResponseEntity<Page<Product>> getProductsBySubCategory(
            @PathVariable Long subCategoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Page<Product> productsPage = productService.getProductsBySubCategory(subCategoryId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(productsPage);
    }

    // --- Products by Category (paginated) ---
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<Product>> getProductsByCategoryId(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Page<Product> productsPage = productService.getProductsByCategoryId(categoryId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(productsPage);
    }

    // --- Slug Based ---
    @GetMapping("/slug/{slug}")
    public ResponseEntity<Product> getProductBySlug(@PathVariable String slug) {
        Product product = productService.getProductBySlug(slug);
        if (product == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(product);
    }

    @GetMapping("/generate-slugs")
    public ResponseEntity<String> generateSlugs() {
        productService.generateMissingSlugs();
        return ResponseEntity.ok("Slug generation completed");
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchAndFilterProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long subCategoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Page<Product> results = productService.searchAndFilterProducts(
                keyword, categoryId, subCategoryId, page, size, sortBy, sortDir
        );
        return ResponseEntity.ok(results);
    }


    // --- Unified Search ---
    @GetMapping("/unified-search")
    public ResponseEntity<SearchResultDTO> unifiedSearch(@RequestParam("query") String query) {
        if (query == null || query.trim().isEmpty()) return ResponseEntity.badRequest().build();
        SearchResultDTO result = productService.unifiedSearch(query.trim());
        return ResponseEntity.ok(result);
    }

    // --- Count ---
    @GetMapping("/count")
    public ResponseEntity<Long> getProductsCount() {
        return ResponseEntity.ok(productService.countAllProducts());
    }

    // --- Nearby ---
    @GetMapping("/nearby")
    public ResponseEntity<List<Product>> getProductsFromNearbyVendors(
            @RequestParam String type,
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "10") double radiusKm,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long subCategoryId
    ) {
        return ResponseEntity.ok(productService.getProductsFromNearbyVendors(type, lat, lng, radiusKm, categoryId, subCategoryId));
    }

    // --- Admin Search ---
    @GetMapping("/admin")
    public ResponseEntity<Page<ProductAdminRowDTO>> searchProductsAdmin(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String vendorType,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long subCategoryId,
            @RequestParam(defaultValue = "ALL") String availability,
            @RequestParam(defaultValue = "ALL") String stockView,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(defaultValue = "10") int lowStockThreshold
    ) {
        return ResponseEntity.ok(
            productService.searchProductsAdmin(q, vendorType, vendorId, categoryId, subCategoryId,
                    availability, stockView, minPrice, maxPrice,
                    page, size, sortBy, sortDir, lowStockThreshold)
        );
    }

    // --- Decrease Stock ---
    @PutMapping("/{id}/decrease-stock")
    public ResponseEntity<String> decreaseStock(@PathVariable Long id, @RequestParam int quantity) {
        productService.decreaseStock(id, quantity);
        return ResponseEntity.ok("Stock decreased successfully");
    }

    // --- Count My Products ---
    @GetMapping("/vendor/my-products/count")
    public ResponseEntity<Long> getMyProductsCount(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(productService.countProductsForCurrentVendor(token));
    }

    // --- Bulk Upload CSV ---
    @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> bulkUploadProducts(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String token
    ) throws IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "file is required"));
        }
        Map<String, Object> result = productService.bulkUploadCsv(file, token);
        return ResponseEntity.ok(result);
    }

    // --- Utility ---
    private Product mapDtoToProduct(ProductRequestDTO dto, SubCategory subCategory) {
        Product p = new Product();
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setBrand(dto.getBrand());
        p.setPrice(dto.getPrice());
        p.setMrp(dto.getMrp());
        p.setDiscount(dto.getDiscount());
        p.setStock(dto.getStock());
        p.setUnit(dto.getUnit());
        p.setIsAvailable(dto.getIsAvailable());
        p.setSubCategory(subCategory);
        p.setImageUrl(dto.getImageUrl());
        if (dto.getProductType() != null) {
            p.setProductType(com.carocart.productservice.entity.ProductType.valueOf(dto.getProductType().toUpperCase()));
        }
        p.setPackSize(dto.getPackSize());
        p.setManufacturer(dto.getManufacturer());
        p.setFssaiLicenseNo(dto.getFssaiLicenseNo());
        p.setCountryOfOrigin(dto.getCountryOfOrigin());
        p.setIngredients(dto.getIngredients());
        p.setNutritionInfo(dto.getNutritionInfo());
        p.setReturnPolicy(dto.getReturnPolicy());
        p.setFoodCategory(dto.getFoodCategory());
        p.setVeg(dto.getVeg());
        p.setRestaurantName(dto.getRestaurantName());
        p.setCustomizationOptions(dto.getCustomizationOptions());
        p.setAddOns(dto.getAddOns());
        p.setBestseller(dto.getBestseller());
        return p;
    }
}
