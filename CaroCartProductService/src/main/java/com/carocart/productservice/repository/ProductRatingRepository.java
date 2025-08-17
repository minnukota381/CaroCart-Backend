package com.carocart.productservice.repository;

import com.carocart.productservice.entity.ProductRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface ProductRatingRepository extends JpaRepository<ProductRating, Long> {
    boolean existsByUserIdAndOrderItemId(Long userId, Long orderItemId);
    List<ProductRating> findByProductIdOrderByCreatedAtDesc(Long productId);

    @Query("select avg(pr.rating) from ProductRating pr where pr.productId = ?1")
    Double calcAvg(Long productId);

    @Query("select count(pr) from ProductRating pr where pr.productId = ?1")
    Long calcCount(Long productId);

    // ADD THIS:
    Optional<ProductRating> findByUserIdAndOrderItemId(Long userId, Long orderItemId);
}
