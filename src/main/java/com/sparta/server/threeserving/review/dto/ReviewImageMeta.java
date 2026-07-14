package com.sparta.server.threeserving.review.dto;

import jakarta.validation.constraints.NotBlank;

public record ReviewImageMeta(
        @NotBlank(message = "이미지 key는 필수입니다.")
        String key,
        String originName,
        int sequence
) {
}
