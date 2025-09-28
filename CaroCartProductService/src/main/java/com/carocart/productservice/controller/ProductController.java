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

import java.io.IOException;
import java.util.List;

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

    // --- Update Product ---
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Product> updateProduct(
        @PathVariable Long id,
        @RequestBody ProductRequestDTO productDTO,
        @RequestHeader("Authorization") String token
    ) throws IOException {

        if (productDTO.getPrice() > productDTO.getMrp()) {
            throw new IllegalArgumentException("Price must not exceed MRP");
        }

        SubCategory subCategory = subCategoryRepository.findById(productDTO.getSubCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Subcategory not found"));

        Product updatedProduct = mapDtoToProduct(productDTO, subCategory);
        Product updated = productService.updateProduct(id, updatedProduct, null, token);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
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
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    // --- Get Product DTO ---
    @GetMapping("/dto/{id}")
    public ResponseEntity<ProductDTO> getProductDTOById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductDTOById(id));
    }

    // --- Get All Products ---
    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // --- Vendor's Products ---
    @GetMapping("/vendor/my-products")
    public ResponseEntity<List<Product>> getVendorProducts(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(productService.getProductsByVendor(token));
    }

    // --- Grouped by Category/Subcategory for a Vendor ---
    @GetMapping("/vendor/{vendorId}/grouped")
    public ResponseEntity<?> getVendorProductsGroupedByCategoryAndSubCategory(@PathVariable Long vendorId) {
        return ResponseEntity.ok(productService.getProductsGroupedByCategoryAndSubCategory(vendorId));
    }

    // --- By SubCategory ---
    @GetMapping("/subcategory/{subCategoryId}")
    public ResponseEntity<List<Product>> getProductsBySubCategory(@PathVariable Long subCategoryId) {
        return ResponseEntity.ok(productService.getProductsBySubCategory(subCategoryId));
    }

    // --- By Category ---
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategoryId(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategoryId(categoryId));
    }

    // --- Slug Based ---
    @GetMapping("/slug/{slug}")
    public ResponseEntity<Product> getProductBySlug(@PathVariable String slug) {
        Product product = productService.getProductBySlug(slug);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @GetMapping("/generate-slugs")
    public ResponseEntity<String> generateSlugs() {
        productService.generateMissingSlugs();
        return ResponseEntity.ok("Slug generation completed");
    }

    // --- Search & Filter ---
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchAndFilterProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long subCategoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) Boolean discounted,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir
    ) {
        List<Product> results = productService.searchAndFilterProducts(
                keyword, categoryId, subCategoryId, minPrice, maxPrice,
                inStock, discounted, sortBy, sortDir
        );
        return ResponseEntity.ok(results);
    }

    // --- Unified Search ---
    @GetMapping("/unified-search")
    public ResponseEntity<SearchResultDTO> unifiedSearch(@RequestParam("query") String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
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
            productService.searchProductsAdmin(
                q, vendorType, vendorId, categoryId, subCategoryId,
                availability, stockView, minPrice, maxPrice,
                page, size, sortBy, sortDir, lowStockThreshold
            )
        );
    }

    @PutMapping("/{id}/decrease-stock")
    public ResponseEntity<String> decreaseStock(@PathVariable Long id, @RequestParam int quantity) {
        productService.decreaseStock(id, quantity);
        return ResponseEntity.ok("Stock decreased successfully");
    }

    // Utility to avoid duplicate mapping
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
        p.setProductType(
            com.carocart.productservice.entity.ProductType.valueOf(dto.getProductType().toUpperCase())
        );
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
    
 // ProductController.java
    @GetMapping("/vendor/my-products/count")
    public ResponseEntity<Long> getMyProductsCount(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(productService.countProductsForCurrentVendor(token));
    }

}
