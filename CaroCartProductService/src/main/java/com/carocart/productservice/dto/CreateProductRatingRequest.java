package com.carocart.productservice.dto;

public class CreateProductRatingRequest {
    private Long orderItemId;
    private int rating;     // 1..5
    private String review;  // optional
    private Long productId;   // was int/long
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }
}
