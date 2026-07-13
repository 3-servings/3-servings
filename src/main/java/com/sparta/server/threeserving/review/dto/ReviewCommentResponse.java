package com.sparta.server.threeserving.review.dto;

import com.sparta.server.threeserving.review.entity.ReviewComment;

import java.time.Instant;
import java.util.UUID;

//사장 답글 응답
public record ReviewCommentResponse(
        UUID id,
        UUID reviewId,
        Long userId,
        String nickname,
        String content,
        Instant createdAt,
        Instant updatedAt
) {
    public ReviewCommentResponse(ReviewComment c) {
        this(
                c.getId(),
                c.getReview().getId(),
                c.getUser().getId(),
                c.getUser().getNickname(),
                c.getContent(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
