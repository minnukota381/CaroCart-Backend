package com.carocart.productservice.controller;

import com.carocart.productservice.dto.ProductDTO;
import com.carocart.productservice.dto.ProductRequestDTO;
import com.carocart.productservice.entity.Product;
import com.carocart.productservice.entity.SubCategory;
import com.carocart.productservice.repository.SubCategoryRepository;
import com.carocart.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Product> addProduct(
        @RequestBody ProductRequestDTO productDTO,
        @RequestHeader("Authorization") String token
    ) throws IOException {

        // Validate price <= mrp
        if (productDTO.getPrice() > productDTO.getMrp()) {
            throw new IllegalArgumentException("Price must not exceed MRP");
        }

        // Validate subcategory exists
        SubCategory subCategory = subCategoryRepository.findById(productDTO.getSubCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Subcategory not found"));

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setBrand(productDTO.getBrand());
        product.setPrice(productDTO.getPrice());
        product.setMrp(productDTO.getMrp());
        product.setDiscount(productDTO.getDiscount());
        product.setStock(productDTO.getStock());
        product.setUnit(productDTO.getUnit());
        product.setIsAvailable(productDTO.getIsAvailable());
        product.setSubCategory(subCategory);

        // Set imageUrl (from Firebase, sent by frontend)
        product.setImageUrl(productDTO.getImageUrl());

        return ResponseEntity.ok(productService.addProduct(product, null, token));
    }

    // === UPDATE PRODUCT ===
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequestDTO productDTO,
            @RequestHeader("Authorization") String token) throws IOException {

        if (productDTO.getPrice() > productDTO.getMrp()) {
            throw new IllegalArgumentException("Price must not exceed MRP");
        }

        SubCategory subCategory = subCategoryRepository.findById(productDTO.getSubCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Subcategory not found"));

        Product updatedProduct = new Product();
        updatedProduct.setName(productDTO.getName());
        updatedProduct.setDescription(productDTO.getDescription());
        updatedProduct.setBrand(productDTO.getBrand());
        updatedProduct.setPrice(productDTO.getPrice());
        updatedProduct.setMrp(productDTO.getMrp());
        updatedProduct.setDiscount(productDTO.getDiscount());
        updatedProduct.setStock(productDTO.getStock());
        updatedProduct.setUnit(productDTO.getUnit());
        updatedProduct.setIsAvailable(productDTO.getIsAvailable());
        updatedProduct.setSubCategory(subCategory);

        // Set new imageUrl (if changed)
        updatedProduct.setImageUrl(productDTO.getImageUrl());

        Product updated = productService.updateProduct(id, updatedProduct, null, token);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    // ✅ Delete product (Admin or Vendor)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id,
                                                @RequestHeader("Authorization") String token) {
        productService.deleteProduct(id, token);
        return ResponseEntity.ok("Product deleted successfully");
    }

    // ✅ Get product by ID (public)
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    // ✅ Get products by subcategory (public)
    @GetMapping("/subcategory/{subCategoryId}")
    public ResponseEntity<List<Product>> getProductsBySubCategory(@PathVariable Long subCategoryId) {
        return ResponseEntity.ok(productService.getProductsBySubCategory(subCategoryId));
    }

    // ✅ Used by CartService/OrderService (internal DTO)
    @GetMapping("/dto/{id}")
    public ResponseEntity<ProductDTO> getProductDTOById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductDTOById(id));
    }

    // ✅ Vendor: View their own products
    @GetMapping("/vendor/my-products")
    public ResponseEntity<List<Product>> getVendorProducts(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(productService.getProductsByVendor(token));
    }

    // ✅ Get all products (public)
    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // ✅ Search and filter products (public)
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

    // ✅ Get products by category ID
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategoryId(@PathVariable Long categoryId) {
        List<Product> products = productService.getProductsByCategoryId(categoryId);
        return ResponseEntity.ok(products);
    }

    // ✅ Get total products count
    @GetMapping("/count")
    public ResponseEntity<Long> getProductsCount() {
        long count = productService.countAllProducts();
        return ResponseEntity.ok(count);
    }
    
 // ✅ Get product by slug (used by React)
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

}
