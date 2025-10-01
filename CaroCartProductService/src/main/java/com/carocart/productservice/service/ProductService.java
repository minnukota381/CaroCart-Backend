package com.carocart.productservice.service;

import com.carocart.productservice.dto.ProductAdminRowDTO;
import com.carocart.productservice.dto.ProductDTO;
import com.carocart.productservice.dto.ProductRequestDTO;
import com.carocart.productservice.dto.SearchResultDTO;
import com.carocart.productservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ProductService {

    // --- Add / Update / Delete ---
    Product addProduct(Product product, MultipartFile image, String token) throws IOException;

    Product updateProduct(Long id, Product product, String token) throws IOException;

    void deleteProduct(Long id, String token);

    void decreaseStock(Long productId, int quantity);

    // --- Get Products ---
    Product getProductById(Long id);

    ProductDTO getProductDTOById(Long id);

    Product getProductBySlug(String slug);

    // --- Pagination-enabled methods ---
    Page<Product> getAllProducts(int page, int size, String sortBy, String sortDir);

    Page<Product> getAllProducts(org.springframework.data.domain.Pageable pageable);

    Page<Product> getProductsByVendor(String token, int page, int size, String sortBy, String sortDir);

    Page<Product> getProductsBySubCategory(Long subCategoryId, int page, int size, String sortBy, String sortDir);

    Page<Product> getProductsByCategoryId(Long categoryId, int page, int size, String sortBy, String sortDir);

    // --- Search / Filter / Group ---
    public Page<Product> searchAndFilterProducts(String keyword, Long categoryId, Long subCategoryId,
            int page, int size, String sortBy, String sortDir) ;

    List<Product> getProductsFromNearbyVendors(String type, double lat, double lng, double radiusKm,
                                               Long categoryId, Long subCategoryId);

    Map<String, Map<String, List<Product>>> getProductsGroupedByCategoryAndSubCategory(Long vendorId);

    Page<ProductAdminRowDTO> searchProductsAdmin(String q, String vendorType, Long vendorId, Long categoryId,
                                                 Long subCategoryId, String availability, String stockView,
                                                 Double minPrice, Double maxPrice, int page, int size,
                                                 String sortBy, String sortDir, int lowStockThreshold);

    // --- Count / Utilities ---
    long countAllProducts();

    long countProductsForCurrentVendor(String token);

    void generateMissingSlugs();

    Map<String, Object> bulkUploadCsv(MultipartFile file, String token) throws IOException;

    SearchResultDTO unifiedSearch(String query);
}
