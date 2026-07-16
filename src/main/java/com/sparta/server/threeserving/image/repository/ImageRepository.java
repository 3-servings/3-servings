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

    List<Image> findAllByDomainTypeAndTargetIdOrderBySequenceAsc(DomainType domainType, UUID targetId);

    List<Image> findByDomainTypeAndTargetIdIn(DomainType domainType, List<UUID> targetIds);

    // 살아있는 이미지 전체 조회
    List<Image> findAllByDomainTypeAndTargetIdAndDeletedAtIsNullOrderBySequenceAsc(DomainType domainType, UUID targetId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Image i SET i.deletedAt = CURRENT_TIMESTAMP, i.deletedBy = :userId WHERE i.domainType = :domainType AND i.targetId = :targetId AND i.deletedAt IS NULL")
    void softDeleteAllByTargetId(@Param("domainType") DomainType domainType, @Param("targetId") UUID targetId, @Param("userId") Long userId);
}
