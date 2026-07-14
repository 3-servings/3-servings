package com.sparta.server.threeserving.review.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.image.domain.entity.Image;
import com.sparta.server.threeserving.review.repository.ImageRepository;
import com.sparta.server.threeserving.review.dto.ReviewImageMeta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class ImageService {

    public static final String REVIEW = "REVIEW";
    private static final int MAX_IMAGES = 5;

    private final ImageRepository imageRepository;
    private final S3PresignService s3PresignService;

    /** [역할5] 업로드 완료된 이미지들의 메타를 p_image 에 저장 (key 검증 포함) */
    @Transactional
    public List<String> saveReviewImages(UUID reviewId, List<ReviewImageMeta> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        if (images.size() > MAX_IMAGES) {
            throw new CustomException(ErrorCode.IMAGE_COUNT_EXCEEDED);
        }

        int sequence = 1;
        for (ReviewImageMeta meta : images) {
            // 우리 버킷/review prefix + 실제 업로드 여부 확인 → 크기·타입 확보
            S3PresignService.ObjectMeta head = s3PresignService.validateAndHead(meta.key(), REVIEW.toLowerCase());

            Image image = Image.builder()
                    .domainType(REVIEW)
                    .targetId(reviewId)
                    .sequence(sequence++)
                    .originName(meta.originName())
                    .storedName(fileName(meta.key()))
                    .imagePath(meta.key())
                    .imageUrl(s3PresignService.publicUrl(meta.key()))
                    .fileSize(head.size())
                    .contentType(head.contentType())
                    .build();
            imageRepository.save(image);
        }
        return getImageUrls(reviewId);
    }

    @Transactional(readOnly = true)
    public List<String> getImageUrls(UUID reviewId) {
        return imageRepository
                .findByDomainTypeAndTargetIdOrderBySequenceAsc(REVIEW, reviewId)
                .stream()
                .map(Image::getImageUrl)
                .toList();
    }

    @Transactional
    public void deleteImages(UUID reviewId) {
        List<Image> images = imageRepository.findByDomainTypeAndTargetIdOrderBySequenceAsc(REVIEW, reviewId);
        imageRepository.deleteAll(images);   // @SQLDelete → soft-delete
        // 물리 삭제 정책이면: images.forEach(i -> s3Client.deleteObject(i.getImagePath()))
    }

    private String fileName(String key) {
        return key.substring(key.lastIndexOf("/") + 1);
    }
}