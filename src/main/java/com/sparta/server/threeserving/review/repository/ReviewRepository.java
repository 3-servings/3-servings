package com.sparta.server.threeserving.review.repository;

import com.sparta.server.threeserving.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByOrder_IdAndDeletedAtIsNull(UUID orderId);

    @EntityGraph(attributePaths = {"order", "user", "store"})
    Optional<Review> findByIdAndDeletedAtIsNull(UUID id);

    // 목록: 닉네임 N+1 방지
    @EntityGraph(attributePaths = {"user"})
    Page<Review> findByStore_IdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID storeId, Pageable pageable);

    @Query("SELECT COALESCE(AVG(r.star), 0) FROM Review r " +   // 가게 평점 재계산
            "WHERE r.store.id = :storeId AND r.deletedAt IS NULL")
    double findAverageStarByStoreId(UUID storeId);

    long countByStore_IdAndDeletedAtIsNull(UUID storeId);

}
