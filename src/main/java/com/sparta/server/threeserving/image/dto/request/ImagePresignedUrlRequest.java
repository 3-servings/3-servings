package com.sparta.server.threeserving.image.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImagePresignedUrlRequest {

    @NotBlank(message = "도메인 타입(ex: MENU, REVIEW)은 필수입니다.")
    private String domainType;

    @NotBlank(message = "원본 파일명은 필수입니다.")
    private String originName;

}
