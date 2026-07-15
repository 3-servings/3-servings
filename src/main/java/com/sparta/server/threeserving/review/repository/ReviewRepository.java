package com.sparta.server.threeserving.review.repository;

import com.sparta.server.threeserving.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // 검색: storeId(선택) + 최소 별점(선택) + 내용 키워드(선택). 정렬/페이지는 Pageable로 주입.
    // 닉네임 N+1 방지를 위해 user 페치.
    @EntityGraph(attributePaths = "user")
    @Query("""
    SELECT r
    FROM Review r
    WHERE r.deletedAt IS NULL
      AND (:storeId IS NULL OR r.store.id = :storeId)
      AND (:minStar IS NULL OR r.star >= :minStar)
      AND (:keyword IS NULL OR r.content LIKE CONCAT('%', :keyword, '%'))
""")
    Page<Review> search(
            @Param("storeId") UUID storeId,
            @Param("minStar") Integer minStar,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
