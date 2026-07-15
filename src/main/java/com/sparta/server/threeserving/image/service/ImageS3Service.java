package com.sparta.server.threeserving.image.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.image.dto.request.ImagePresignedUrlRequest;
import com.sparta.server.threeserving.image.dto.response.ImagePresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageS3Service {

    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.s3.base-url}") // FE가 직접 접근할 이미지의 base url
    private String s3BaseUrl;

    public ImagePresignedUrlResponse generatePresignedUrl(ImagePresignedUrlRequest request) {

        String originName = request.getOriginName();

        // 확장자 존재 여부 검증
        int lastDotIndex = originName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            log.warn("Invalid file name without extension: {}", originName);
            throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION);
        }

        // 허용된 확장자 검증
        String extension = originName.substring(lastDotIndex).toLowerCase();
        List<String> allowedExtensions = List.of(".jpg", ".jpeg", ".png", ".webp");
        if (!allowedExtensions.contains(extension)) {
            log.warn("Unsupported file extension requested: {}", extension);
            throw new CustomException(ErrorCode.UNSUPPORTED_FILE_FORMAT);
        }

        String storedName = UUID.randomUUID().toString() + extension;
        // S3 내부에 저장될 경로 (도메인별 폴더링 ex: MENU/UUID.jpg)
        String imagePath = request.getDomainType().toUpperCase() + "/" + storedName;
        String finalImageUrl = s3BaseUrl + "/" + imagePath;   // 최종 이미지 url

        try{
            // S3 업로드 요청 메타데이터 생성
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(imagePath)
                    .build();

            // Presigned URL 발급 설정
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(5))
                    .putObjectRequest(objectRequest)
                    .build();

            // 임시 URL 발급
            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            log.info("S3 Presigned URL generated - Domain: {}, Path: {}", request.getDomainType(), imagePath);
            return ImagePresignedUrlResponse.of(presignedUrl, storedName, imagePath, finalImageUrl);

        } catch (Exception e) {
            log.error("Failed to generate S3 Presigned URL for originName: {}", request.getOriginName(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
