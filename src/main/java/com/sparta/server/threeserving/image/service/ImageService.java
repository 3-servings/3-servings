package com.sparta.server.threeserving.image.service;

import com.sparta.server.threeserving.image.dto.request.ImageRequest;
import com.sparta.server.threeserving.image.entity.DomainType;
import com.sparta.server.threeserving.image.entity.Image;
import com.sparta.server.threeserving.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;

    // 단건 이미지 저장
    @Transactional
    public String saveImage(DomainType domainType, UUID targetId, ImageRequest request) {
        if (request == null) return null;

        Image image = Image.builder()
                .domainType(domainType)
                .targetId(targetId)
                .sequence(1) // 단건은 항상 sequence 1 고정
                .originName(request.getOriginName())
                .storedName(request.getStoredName())
                .imagePath(request.getImagePath())
                .imageUrl(request.getImageUrl())
                .fileSize(request.getFileSize())
                .contentType(request.getContentType())
                .build();

        imageRepository.save(image);
        return image.getImageUrl();
    }

    // 다건 이미지 일괄 저장
    @Transactional
    public List<String> saveImages(DomainType domainType, UUID targetId, List<ImageRequest> requests) {
        if (requests == null || requests.isEmpty()) return Collections.emptyList();

        List<String> imageUrls = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            ImageRequest req = requests.get(i);
            Image image = Image.builder()
                    .domainType(domainType)
                    .targetId(targetId)
                    .sequence(i + 1) // 1번부터 순차적으로 sequence 부여
                    .originName(req.getOriginName())
                    .storedName(req.getStoredName())
                    .imagePath(req.getImagePath())
                    .imageUrl(req.getImageUrl())
                    .fileSize(req.getFileSize())
                    .contentType(req.getContentType())
                    .build();

            imageRepository.save(image);
            imageUrls.add(image.getImageUrl());
        }
        return imageUrls;
    }

    // 단건 이미지 교체
    @Transactional
    public String replaceImage(DomainType domainType, UUID targetId, ImageRequest request, Long userId) {
        if (request == null) return null;

        softDeleteImages(domainType, targetId, userId);
        return saveImage(domainType, targetId, request);
    }

    // 다건 이미지 일괄 교체
    @Transactional
    public List<String> replaceImages(DomainType domainType, UUID targetId, List<ImageRequest> requests, Long userId) {
        softDeleteImages(domainType, targetId, userId);
        return saveImages(domainType, targetId, requests);
    }

    // 특정 targetId의 모든 이미지 일괄 삭제
    @Transactional
    public void softDeleteImages(DomainType domainType, UUID targetId, Long userId) {
        List<Image> images = imageRepository.findAllByDomainTypeAndTargetIdOrderBySequenceAsc(domainType, targetId);
        for (Image image : images) {
            image.softDelete(userId);
        }
    }

    // 단건 이미지 조회
    @Transactional(readOnly = true)
    public String getImageUrl(DomainType domainType, UUID targetId) {
        return imageRepository.findAllByDomainTypeAndTargetIdOrderBySequenceAsc(domainType, targetId)
                .stream()
                .findFirst()
                .map(Image::getImageUrl)
                .orElse(null);
    }

    // 이미지 다건 조회, N+1 고려
    @Transactional(readOnly = true)
    public Map<UUID, String> getImageUrlMap(DomainType domainType, List<UUID> targetIds) {
        if (targetIds.isEmpty()) return Collections.emptyMap();

        List<Image> images = imageRepository.findByDomainTypeAndTargetIdIn(domainType, targetIds);
        return images.stream()
                .collect(Collectors.toMap(
                        Image::getTargetId,
                        Image::getImageUrl,
                        (img1, img2) -> img1
                ));
    }
}
