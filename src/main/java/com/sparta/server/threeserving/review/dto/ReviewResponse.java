package com.sparta.server.threeserving.review.dto;

import com.sparta.server.threeserving.review.entity.Review;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReviewResponse (
    UUID id,
    UUID orderId,
    Long userId,
    String nickname,
    UUID storeId,
    int star,
    String content,
    ReviewCommentResponse ownerReply, // 사장 답글 (없으면 null)
    Instant createdAt,
    Instant updatedAt,
    List<String> imageUrls
){
    //작성, 수정 직후에 (답글은 널)
    public ReviewResponse(Review review, List<String> imageUrls){
        this(review, imageUrls, null);
    }

    //상세 조회 (이미지 + 답글 포함 가능)
    public ReviewResponse(Review review, List<String> imageUrls, ReviewCommentResponse ownerReply){
        this(
                review.getId(),
                review.getOrder().getId(),
                review.getUser().getId(),
                review.getUser().getNickname(),
                review.getStore().getId(),
                review.getStar(),
                review.getContent(),
                ownerReply,
                review.getCreatedAt(),
                review.getUpdatedAt(),
                imageUrls == null ? List.of() : imageUrls
        );
    }
}
