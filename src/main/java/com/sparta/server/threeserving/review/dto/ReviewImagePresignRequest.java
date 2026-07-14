package com.sparta.server.threeserving.review.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReviewImagePresignRequest(
        @NotEmpty(message = "발급할 파일 정보가 필요합니다.")
        @Size(max = 5, message = "이미지는 최대 5장입니다.")
        List<@Valid FileMeta> files
) {
    public record FileMeta(
            @NotBlank(message = "파일명은 필수입니다.")
            String originName,
            @NotBlank(message = "콘텐츠 타입은 필수입니다.")
            String contentType   // 예: image/jpeg
    ) {}
}
