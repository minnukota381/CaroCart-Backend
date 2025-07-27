package com.carocart.productservice.service;

import com.carocart.productservice.dto.ProductDTO;
import com.carocart.productservice.entity.Product;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;


public interface ProductService {
	Product addProduct(Product product, MultipartFile image, String token) throws IOException;
   
    List<Product> getAllProducts();
    Product getProductById(Long id);
    Product updateProduct(Long id, Product product, MultipartFile image, String token) throws IOException;
    void deleteProduct(Long id, String token);
    ProductDTO getProductDTOById(Long id);
    List<Product> getProductsBySubCategory(Long subCategoryId);
    List<Product> getProductsByVendor(String token);
    Page<Product> getAllProducts(Pageable pageable);
    List<Product> searchAndFilterProducts(String keyword, Long categoryId, Long subCategoryId,
            Double minPrice, Double maxPrice, Boolean inStock,
            Boolean discounted, String sortBy, String sortDir);
	List<Product> getProductsByCategoryId(Long categoryId);

	long countAllProducts();
	
	Product getProductBySlug(String slug);
    public void generateMissingSlugs();
    
 // ProductService.java

    List<Product> getProductsFromNearbyVendors(String type, double lat, double lng, double radiusKm, Long categoryId, Long subCategoryId);
    Map<String, Map<String, List<Product>>> getProductsGroupedByCategoryAndSubCategory(Long vendorId);

}
