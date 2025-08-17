package com.carocart.productservice.dto;

import java.time.LocalDateTime;

public class ProductRatingResponse {
    private Long id;
    private Long userId;
    private Long productId;
    private int rating;
    private String review;
    private LocalDateTime createdAt;

    public ProductRatingResponse(Long id, Long userId, Long productId, int rating, String review, LocalDateTime createdAt) {
        this.id = id; this.userId = userId; this.productId = productId; this.rating = rating; this.review = review; this.createdAt = createdAt;
    }
    // getters...
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getProductId() { return productId; }
    public int getRating() { return rating; }
    public String getReview() { return review; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
