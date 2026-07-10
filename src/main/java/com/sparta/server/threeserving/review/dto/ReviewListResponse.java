package com.sparta.server.threeserving.review.dto;

import com.sparta.server.threeserving.review.entity.Review;

import java.time.Instant;
import java.util.UUID;

public record ReviewListResponse(
        UUID id,
        Long userId,
        String nickname,
        int star,
        String content,
        Instant createdAt
) {
    public ReviewListResponse(Review review){
        this(
                review.getId(),
                review.getUser().getId(),
                review.getUser().getNickname(),
                review.getStar(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}
