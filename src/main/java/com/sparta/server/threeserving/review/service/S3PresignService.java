package com.sparta.server.threeserving.review.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class S3PresignService {

    private static final Duration PRESIGN_TTL = Duration.ofMinutes(5);

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region}")
    private String region;

    // LocalStack 등 로컬 엔드포인트. 있으면 path-style URL 사용
    @Value("${cloud.aws.s3.endpoint:}")
    private String endpoint;

    /** 발급 결과 (프론트 전달용) */
    public record PresignedItem(String key, String uploadUrl, String publicUrl) {}

    /** headObject 결과 (메타 저장용) */
    public record ObjectMeta(long size, String contentType) {}

    // ===== [역할2] Presigned PUT URL 발급 =====
    public PresignedItem createPutPresign(String dir, String originName, String contentType) {
        validateContentType(contentType);

        String key = dir + "/" + UUID.randomUUID() + extractExt(originName);   // review/uuid.jpg

        // 서명에 content-type 을 포함 → 프론트는 같은 Content-Type 헤더로 PUT 해야 함(타입 강제)
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(PRESIGN_TTL)
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);
        return new PresignedItem(key, presigned.url().toString(), publicUrl(key));
    }

    // ===== [역할3] 우리 버킷/경로 검증 + 실제 업로드 여부(headObject) 확인 =====
    public ObjectMeta validateAndHead(String key, String allowedPrefix) {
        // 1) 경로(prefix) 검증 — 클라이언트가 임의 key 를 넣지 못하게
        if (key == null || !key.startsWith(allowedPrefix + "/")) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_PATH);
        }
        // 2) 실제 업로드됐는지 우리 버킷에서 확인 (없으면 예외)
        try {
            HeadObjectResponse head = s3Client.headObject(
                    HeadObjectRequest.builder().bucket(bucket).key(key).build());
            String ct = head.contentType();
            validateContentType(ct);   // 사후 타입 검증
            return new ObjectMeta(head.contentLength(), ct);
        } catch (S3Exception e) {
            log.warn("headObject 실패 key={}, status={}", key, e.statusCode());
            throw new CustomException(ErrorCode.IMAGE_NOT_UPLOADED);   // 404 등
        }
    }

    public String publicUrl(String key) {
        if (endpoint != null && !endpoint.isBlank()) {
            return endpoint + "/" + bucket + "/" + key;   // LocalStack path-style
        }
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    private void validateContentType(String contentType) {
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_TYPE);
        }
    }

    private String extractExt(String origin) {
        if (origin == null || !origin.contains(".")) return "";
        return origin.substring(origin.lastIndexOf("."));
    }
}