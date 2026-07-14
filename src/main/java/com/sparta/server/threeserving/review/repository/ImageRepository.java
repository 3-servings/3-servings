package com.sparta.server.threeserving.review.repository;

import com.sparta.server.threeserving.image.domain.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {

    // @SQLRestriction 덕분에 삭제된 이미지는 자동 제외
    List<Image> findByDomainTypeAndTargetIdOrderBySequenceAsc(String domainType, UUID targetId);

}
