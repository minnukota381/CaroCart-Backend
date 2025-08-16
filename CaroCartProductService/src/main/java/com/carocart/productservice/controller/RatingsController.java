package com.carocart.productservice.controller;

import com.carocart.productservice.dto.CreateProductRatingRequest;
import com.carocart.productservice.dto.ProductRatingResponse;
import com.carocart.productservice.dto.RatingEligibilityDTO;
import com.carocart.productservice.service.RatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.Map;

@RestController
@RequestMapping("/products/{productId}/ratings")
public class RatingsController {

    private final RatingService ratingService;

    public RatingsController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    /** Create a rating for a delivered order item (auth required). */
    @PostMapping
    public ResponseEntity<Long> create(
            @PathVariable Long productId,
            @RequestBody CreateProductRatingRequest body,
            @RequestHeader("Authorization") String token) {

        // If client didn’t send productId in the body, set it from the path.
        // If they did, make sure it matches the path to avoid mistakes.
        if (body.getProductId() == null) {
            body.setProductId(productId);
        } else if (!Objects.equals(body.getProductId(), productId)) {
            throw new IllegalArgumentException("Product mismatch between path and body");
        }

        Long id = ratingService.create(token, body);
        return ResponseEntity.ok(id);
    }

    /** List ratings for a product (public). */
    @GetMapping
    public ResponseEntity<List<ProductRatingResponse>> list(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ratingService.list(productId, page, size));
    }

    /** Get the current user's rating for a specific order item (to prefill edit UI). */

    @GetMapping("/mine")
    public ResponseEntity<ProductRatingResponse> mine(
        @PathVariable Long productId,
        @RequestParam Long orderItemId,
        @RequestHeader(value = "Authorization", required = false) String token) {
      Optional<ProductRatingResponse> mine = ratingService.getMine(token, productId, orderItemId);
      return mine.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }


    /** Update your own rating (auth required). */
    @PutMapping("/{ratingId}")
    public ResponseEntity<ProductRatingResponse> update(
            @PathVariable Long productId,
            @PathVariable Long ratingId,
            @RequestBody CreateProductRatingRequest body, // use rating + review fields
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(
                ratingService.update(token, productId, ratingId, body.getRating(), body.getReview())
        );
    }

    /** Delete your own rating (auth required). */
    @DeleteMapping("/{ratingId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long productId,
            @PathVariable Long ratingId,
            @RequestHeader("Authorization") String token) {
        ratingService.delete(token, productId, ratingId);
        return ResponseEntity.noContent().build();
    }

    /** Check if the current user is allowed to rate this product for a given order item. */
    @GetMapping("/eligibility")
    public ResponseEntity<RatingEligibilityDTO> eligibility(
            @PathVariable Long productId,
            @RequestParam Long orderId,
            @RequestParam Long orderItemId,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(
                ratingService.checkEligibility(token, productId, orderId, orderItemId)
        );
    }

    /** Small summary (avg, count, distribution) to show on PDP/PLP. */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary(@PathVariable Long productId) {
        return ResponseEntity.ok(ratingService.summary(productId));
    }
}
