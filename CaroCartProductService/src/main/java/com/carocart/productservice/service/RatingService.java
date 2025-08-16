package com.carocart.productservice.service;

import com.carocart.productservice.dto.CreateProductRatingRequest;
import com.carocart.productservice.dto.ProductRatingResponse;
import com.carocart.productservice.dto.RatingEligibilityDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RatingService {
//    ProductRatingResponse create(String token, Long productId, CreateProductRatingRequest req);
    Long create(String userBearerToken, CreateProductRatingRequest req);
    List<ProductRatingResponse> list(Long productId, int page, int size);
    Optional<ProductRatingResponse> getMine(String token, Long productId, Long orderItemId);
    ProductRatingResponse update(String token, Long productId, Long ratingId, Integer rating, String review);
    void delete(String token, Long productId, Long ratingId);
    RatingEligibilityDTO checkEligibility(String token, Long productId, Long orderId, Long orderItemId);
    Map<String, Object> summary(Long productId);
}
