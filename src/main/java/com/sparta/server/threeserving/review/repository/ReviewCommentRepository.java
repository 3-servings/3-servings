package com.sparta.server.threeserving.review.repository;

import com.sparta.server.threeserving.review.entity.ReviewComment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, UUID> {

    // 답글 중복 방지(리뷰당 1개)
    boolean existsByReview_IdAndDeletedAtIsNull(UUID reviewId);

    @EntityGraph(attributePaths = {"user"})
    Optional<ReviewComment> findByReview_IdAndDeletedAtIsNull(UUID reviewId);
}
