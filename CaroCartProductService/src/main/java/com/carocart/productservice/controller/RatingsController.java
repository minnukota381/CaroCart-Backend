package com.carocart.productservice.controller;

import com.carocart.productservice.dto.CreateProductRatingRequest;
import com.carocart.productservice.dto.ProductRatingResponse;
import com.carocart.productservice.dto.RatingEligibilityDTO;
import com.carocart.productservice.service.RatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/products/{productId}/ratings")
public class RatingsController {

    private final RatingService ratingService;

    public RatingsController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    private static String normalizeBearer(String token) {
        if (token == null || token.isBlank()) return null;
        return token.startsWith("Bearer ") ? token.substring(7) : token;
    }

    /** Create a rating for a delivered order item (auth required). */
    @PostMapping
    public ResponseEntity<?> create(
            @PathVariable Long productId,
            @RequestBody CreateProductRatingRequest body,
            @RequestHeader(value = "Authorization", required = false) String token) {

        try {
            String jwt = normalizeBearer(token);
            if (jwt == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

            if (body.getProductId() == null) {
                body.setProductId(productId);
            } else if (!Objects.equals(body.getProductId(), productId)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Product mismatch between path and body"));
            }

            Long id = ratingService.create("Bearer " + jwt, body);
            return ResponseEntity.ok(id);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            // e.g., order not delivered yet, or already rated
            return ResponseEntity.status(409).body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Internal error"));
        }
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
    public ResponseEntity<?> mine(
            @PathVariable Long productId,
            @RequestParam Long orderItemId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String jwt = normalizeBearer(token);
            if (jwt == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

            Optional<ProductRatingResponse> mine = ratingService.getMine("Bearer " + jwt, productId, orderItemId);
            return mine.<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.noContent().build());
        } catch (NoSuchElementException e) {
            // e.g., order item not found or doesn’t belong to the user
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            // e.g., not delivered yet
            return ResponseEntity.status(409).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Internal error"));
        }
    }

    /** Update your own rating (auth required). */
    @PutMapping("/{ratingId}")
    public ResponseEntity<?> update(
            @PathVariable Long productId,
            @PathVariable Long ratingId,
            @RequestBody CreateProductRatingRequest body,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String jwt = normalizeBearer(token);
            if (jwt == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

            ProductRatingResponse resp =
                    ratingService.update("Bearer " + jwt, productId, ratingId, body.getRating(), body.getReview());
            return ResponseEntity.ok(resp);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Internal error"));
        }
    }

    /** Delete your own rating (auth required). */
    @DeleteMapping("/{ratingId}")
    public ResponseEntity<?> delete(
            @PathVariable Long productId,
            @PathVariable Long ratingId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String jwt = normalizeBearer(token);
            if (jwt == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

            ratingService.delete("Bearer " + jwt, productId, ratingId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Internal error"));
        }
    }

    /** Check if the current user is allowed to rate this product for a given order item. */
    @GetMapping("/eligibility")
    public ResponseEntity<?> eligibility(
            @PathVariable Long productId,
            @RequestParam Long orderId,
            @RequestParam Long orderItemId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String jwt = normalizeBearer(token);
            if (jwt == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

            RatingEligibilityDTO dto =
                    ratingService.checkEligibility("Bearer " + jwt, productId, orderId, orderItemId);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            // Not eligible yet (e.g., not delivered)
            return ResponseEntity.status(409).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Internal error"));
        }
    }

    /** Small summary (avg, count, distribution) to show on PDP/PLP. */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary(@PathVariable Long productId) {
        return ResponseEntity.ok(ratingService.summary(productId));
    }
}
