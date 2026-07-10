package com.sparta.server.threeserving.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReviewUpdateRequest (
        @NotNull(message = "별점은 필수입니다.")
        @Min(1)
        @Max(5)
        Integer star,

        String content
){
}
