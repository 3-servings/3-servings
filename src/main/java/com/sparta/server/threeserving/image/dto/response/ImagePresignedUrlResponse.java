package com.sparta.server.threeserving.image.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImagePresignedUrlResponse {

    private String presignedUrl; // FE가 업로드용으로 사용할 임시 URL
    private String storedName;   // S3에 저장될 난수화된 파일명 (UUID.jpg)
    private String imagePath;    // S3 내부 경로 (ex: MENU/UUID.jpg)
    private String imageUrl;     // 프론트엔드가 최종적으로 사용할 CDN URL

    public static ImagePresignedUrlResponse of(String presignedUrl, String storedName, String imagePath, String imageUrl) {
        return ImagePresignedUrlResponse.builder()
                .presignedUrl(presignedUrl)
                .storedName(storedName)
                .imagePath(imagePath)
                .imageUrl(imageUrl)
                .build();
    }
}
