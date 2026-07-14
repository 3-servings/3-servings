package com.sparta.server.threeserving.image.repository;

import com.sparta.server.threeserving.image.entity.DomainType;
import com.sparta.server.threeserving.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {

    List<Image> findAllByDomainTypeAndTargetIdOrderBySequenceAsc(DomainType domainType, UUID targetId);

    List<Image> findByDomainTypeAndTargetIdIn(DomainType domainType, List<UUID> targetIds);
}
