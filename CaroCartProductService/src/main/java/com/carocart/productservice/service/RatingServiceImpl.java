package com.carocart.productservice.service;

import com.carocart.productservice.dto.CreateProductRatingRequest;
import com.carocart.productservice.dto.ProductRatingResponse;
import com.carocart.productservice.dto.RatingEligibilityDTO;
import com.carocart.productservice.entity.Product;
import com.carocart.productservice.entity.ProductRating;
import com.carocart.productservice.feign.OrdersInternalClient;
import com.carocart.productservice.repository.ProductRatingRepository;
import com.carocart.productservice.repository.ProductRepository;
import com.carocart.productservice.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RatingServiceImpl implements RatingService {

    private final ProductRatingRepository ratingRepo;
    private final ProductRepository productRepo;
    private final OrdersInternalClient ordersClient;
    private final JwtUtil jwtUtil;

    public RatingServiceImpl(ProductRatingRepository ratingRepo,
                             ProductRepository productRepo,
                             OrdersInternalClient ordersClient,
                             JwtUtil jwtUtil) {
        this.ratingRepo = ratingRepo;
        this.productRepo = productRepo;
        this.ordersClient = ordersClient;
        this.jwtUtil = jwtUtil;
    }

    // ---------- Helpers ----------

    private ProductRatingResponse toResponse(ProductRating r) {
        return new ProductRatingResponse(
                r.getId(),
                r.getUserId(),
                r.getProductId(),
                r.getRating(),
                r.getReview(),
                r.getCreatedAt()
        );
    }

    /** Recompute product aggregates (avg & count) after any mutation. */
    private void recomputeProductAggregates(Long productId) {
        Double avg = ratingRepo.calcAvg(productId);
        Long count = ratingRepo.calcCount(productId);
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        product.setAvgRating(avg != null ? Math.round(avg * 100.0) / 100.0 : 0.0);
        product.setRatingCount(count != null ? count.intValue() : 0);
        productRepo.save(product);
    }

    /** Verifies delivered + returns mini info from OrderService using a SYSTEM bearer. */
    private RatingEligibilityDTO fetchMiniAndVerifyDelivered(Long orderItemId) {
        String sysBearer = jwtUtil.generateInternalBearer();
        RatingEligibilityDTO mini = ordersClient.getMini(sysBearer, orderItemId);
        if (mini == null) throw new IllegalArgumentException("Order item not found");

        boolean delivered = "DELIVERED".equalsIgnoreCase(mini.getItemStatus())
                || "DELIVERED".equalsIgnoreCase(mini.getOrderStatus());
        if (!delivered) throw new IllegalStateException("Item not delivered yet");

        return mini;
    }

    // ---------- API ----------

    @Override
    @Transactional
    public Long create(String userBearerToken, CreateProductRatingRequest req) {
        if (req.getOrderItemId() == null) {
            throw new IllegalArgumentException("orderItemId is required");
        }
        if (req.getRating() < 1 || req.getRating() > 5) {
            throw new IllegalArgumentException("rating must be 1..5");
        }

        // 1) Ask OrderService (SYSTEM token) for minimal info
        String sysBearer = jwtUtil.generateInternalBearer();
        RatingEligibilityDTO mini = ordersClient.getMini(sysBearer, req.getOrderItemId());
        if (mini == null) throw new IllegalArgumentException("Order item not found");

        // 2) (Optional) verify product match if client sent productId
        // If your DTO keeps a primitive int, replace the next two lines with:
        // long pidReq = req.getProductId();  // 0 means "not provided"
        // if (pidReq > 0 && !Objects.equals(mini.getProductId(), pidReq)) { ... }
        Long pidReq = req.getProductId();
        if (pidReq != null && !pidReq.equals(mini.getProductId())) {
            throw new IllegalArgumentException("Order item does not belong to the given product");
        }

        // 3) must be delivered
        String itemSt = mini.getItemStatus();
        String orderSt = mini.getOrderStatus();
        boolean delivered =
            (itemSt != null && itemSt.equalsIgnoreCase("DELIVERED")) ||
            (orderSt != null && orderSt.equalsIgnoreCase("DELIVERED"));
        if (!delivered) throw new IllegalStateException("Item not delivered yet");

        // 4) prevent duplicate
        Long userId = mini.getUserId();
        if (ratingRepo.existsByUserIdAndOrderItemId(userId, req.getOrderItemId())) {
            throw new IllegalStateException("You already rated this item");
        }

        // 5) persist
        ProductRating pr = new ProductRating();
        pr.setUserId(userId);
        pr.setOrderItemId(req.getOrderItemId());
        pr.setOrderId(mini.getOrderId());
        pr.setProductId(mini.getProductId());
        pr.setRating(req.getRating());
        pr.setReview(req.getReview());
        Long id = ratingRepo.save(pr).getId();

        // 6) update aggregates
        updateProductAggregates(mini.getProductId());
        return id;
    }

    private void updateProductAggregates(Long productId) {
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Double avg = ratingRepo.calcAvg(productId);
        Long count = ratingRepo.calcCount(productId);

        product.setAvgRating(avg != null ? Math.round(avg * 100.0) / 100.0 : 0.0);
        product.setRatingCount(count != null ? count.intValue() : 0);
        productRepo.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductRatingResponse> list(Long productId, int page, int size) {
        List<ProductRating> all = ratingRepo.findByProductIdOrderByCreatedAtDesc(productId);
        int from = Math.max(0, page * size);
        int to = Math.min(all.size(), from + size);
        if (from >= to) return List.of();
        return all.subList(from, to).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductRatingResponse> getMine(String userBearerToken, Long productId, Long orderItemId) {
        String sysBearer = jwtUtil.generateInternalBearer();
        RatingEligibilityDTO mini = ordersClient.getMini(sysBearer, orderItemId);
        if (mini == null) throw new IllegalArgumentException("Order item not found");

        // If order-item doesn't belong to this product, return empty (204)
        if (!Objects.equals(productId, mini.getProductId())) return Optional.empty();

        return ratingRepo.findByUserIdAndOrderItemId(mini.getUserId(), orderItemId)
                .map(this::toResponse);
    }


    @Override
    @Transactional
    public ProductRatingResponse update(String userBearerToken, Long productId, Long ratingId, Integer rating, String review) {
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException("rating must be in 1..5");
        }

        ProductRating r = ratingRepo.findById(ratingId)
                .orElseThrow(() -> new IllegalArgumentException("Rating not found"));
        if (!Objects.equals(r.getProductId(), productId)) {
            throw new IllegalArgumentException("Product mismatch");
        }

        // Ensure caller is owner (via order-item mini)
        RatingEligibilityDTO mini = fetchMiniAndVerifyDelivered(r.getOrderItemId());
        if (!Objects.equals(mini.getUserId(), r.getUserId())) {
            throw new IllegalStateException("You are not allowed to modify this rating");
        }

        if (rating != null) r.setRating(rating);
        if (review != null) r.setReview(review);

        ProductRating saved = ratingRepo.save(r);
        recomputeProductAggregates(productId);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(String userBearerToken, Long productId, Long ratingId) {
        ProductRating r = ratingRepo.findById(ratingId)
                .orElseThrow(() -> new IllegalArgumentException("Rating not found"));
        if (!Objects.equals(r.getProductId(), productId)) {
            throw new IllegalArgumentException("Product mismatch");
        }

        RatingEligibilityDTO mini = fetchMiniAndVerifyDelivered(r.getOrderItemId());
        if (!Objects.equals(mini.getUserId(), r.getUserId())) {
            throw new IllegalStateException("You are not allowed to delete this rating");
        }

        ratingRepo.deleteById(ratingId);
        recomputeProductAggregates(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public RatingEligibilityDTO checkEligibility(String userBearerToken, Long productId, Long orderId, Long orderItemId) {
        RatingEligibilityDTO mini = fetchMiniAndVerifyDelivered(orderItemId);

        boolean productOk = Objects.equals(productId, mini.getProductId());
        boolean alreadyRated = ratingRepo.existsByUserIdAndOrderItemId(mini.getUserId(), orderItemId);

        RatingEligibilityDTO resp = new RatingEligibilityDTO();
        resp.setOrderId(mini.getOrderId());
        resp.setOrderItemId(mini.getOrderItemId());
        resp.setProductId(mini.getProductId());
        resp.setUserId(mini.getUserId());
        resp.setItemStatus(mini.getItemStatus());
        resp.setOrderStatus(mini.getOrderStatus());

        if (!productOk) {
            resp.setEligible(false);
            resp.setReason("Product mismatch");
            return resp;
        }
        if (alreadyRated) {
            resp.setEligible(false);
            resp.setReason("Already rated");
            return resp;
        }
        resp.setEligible(true);
        resp.setReason("OK");
        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> summary(Long productId) {
        Double avg = ratingRepo.calcAvg(productId);
        Long count = ratingRepo.calcCount(productId);

        // simple distribution (compute from full list to avoid extra repo methods)
        List<ProductRating> all = ratingRepo.findByProductIdOrderByCreatedAtDesc(productId);
        Map<Integer, Long> dist = all.stream()
                .collect(Collectors.groupingBy(ProductRating::getRating, Collectors.counting()));
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) {
            distribution.put(String.valueOf(i), dist.getOrDefault(i, 0L));
        }

        // recent few
        List<ProductRatingResponse> recent = all.stream()
                .limit(3)
                .map(this::toResponse)
                .toList();

        Map<String, Object> map = new HashMap<>();
        map.put("avg", avg != null ? Math.round(avg * 100.0) / 100.0 : 0.0);
        map.put("count", count != null ? count : 0L);
        map.put("distribution", distribution);
        map.put("recent", recent);
        return map;
    }
}
