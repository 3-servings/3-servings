package com.sparta.server.threeserving.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReviewCreateRequest(
        @NotNull(message = "주문 ID는 필수입니다.")
        UUID orderId,

        @NotNull(message = "별점은 필수입니다.")
        @Min(value = 1, message = "별점은 1점이상이어야 합니다.")
        @Max(value = 5, message = "별점은 5점 이하여야 합니다.")
        Integer star,

        String content
){}
