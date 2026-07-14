package com.sparta.server.threeserving.review.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReviewUpdateRequest (
        @NotNull(message = "별점은 필수입니다.")
        @Min(1)
        @Max(5)
        Integer star,

        String content,

        // 이미지 교체용 (넘어오면 기존 삭제 후 재저장, 없으면 유지)
        @Size(max = 3, message = "이미지는 최대 3장입니다.")
        List<@Valid ReviewImageMeta> images
){
}
