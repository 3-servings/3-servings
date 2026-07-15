package com.sparta.server.threeserving.image.repository;

import com.sparta.server.threeserving.image.enums.DomainType;
import com.sparta.server.threeserving.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {

    // 조회는 살아있는 이미지만. deletedAt 조건이 없으면 replaceImages(softDelete 후 재저장) 이후
    // 삭제된 옛 이미지가 sequence 순서상 먼저 잡혀 대표 이미지로 노출된다.
    List<Image> findAllByDomainTypeAndTargetIdAndDeletedAtIsNullOrderBySequenceAsc(DomainType domainType, UUID targetId);

    List<Image> findByDomainTypeAndTargetIdInAndDeletedAtIsNullOrderBySequenceAsc(DomainType domainType, List<UUID> targetIds);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Image i SET i.deletedAt = CURRENT_TIMESTAMP, i.deletedBy = :userId WHERE i.domainType = :domainType AND i.targetId = :targetId AND i.deletedAt IS NULL")
    void softDeleteAllByTargetId(@Param("domainType") DomainType domainType, @Param("targetId") UUID targetId, @Param("userId") Long userId);
}
