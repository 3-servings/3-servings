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
        Instant createdAt,
        String thumbnailUrl   // 대표 이미지(첫 장), 없으면 null
) {
    public ReviewListResponse(Review review, String thumbnailUrl){
        this(
                review.getId(),
                review.getUser().getId(),
                review.getUser().getNickname(),
                review.getStar(),
                review.getContent(),
                review.getCreatedAt(),
                thumbnailUrl
        );
    }
}
