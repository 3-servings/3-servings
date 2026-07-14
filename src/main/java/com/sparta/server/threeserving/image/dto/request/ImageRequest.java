package com.sparta.server.threeserving.image.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageRequest {

    @NotBlank(message = "원본 파일명은 필수입니다.")
    private String originName;

    @NotBlank(message = "저장된 파일명은 필수입니다.")
    private String storedName;

    @NotBlank(message = "S3 이미지 경로는 필수입니다.")
    private String imagePath;

    @NotBlank(message = "이미지 URL은 필수입니다.")
    private String imageUrl;

    @PositiveOrZero(message = "파일 크기는 0 이상이어야 합니다.")
    private long fileSize;

    @NotBlank(message = "파일 타입(Content-Type)은 필수입니다.")
    private String contentType;

}
