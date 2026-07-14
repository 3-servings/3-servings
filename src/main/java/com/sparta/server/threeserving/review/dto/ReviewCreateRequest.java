package com.sparta.server.threeserving.review.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ReviewCreateRequest(
        @NotNull(message = "주문 ID는 필수입니다.")
        UUID orderId,

        @NotNull(message = "별점은 필수입니다.")
        @Min(value = 1, message = "별점은 1점이상이어야 합니다.")
        @Max(value = 5, message = "별점은 5점 이하여야 합니다.")
        Integer star,

        String content,

        // 업로드 완료된 이미지들(없으면 빈 배열/null)
        @Size(max = 3, message = "이미지는 최대 3장입니다.")
        List<@Valid ReviewImageMeta> images
){}
