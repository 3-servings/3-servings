package com.sparta.server.threeserving.menu.repository;

import com.sparta.server.threeserving.menu.entity.OptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OptionGroupRepository extends JpaRepository<OptionGroup, UUID> {

    // 특정 가게의 옵션 그룹 전체 조회
    List<OptionGroup> findAllByStoreId(UUID storeId);

    boolean existsByStoreIdAndName(UUID storeId, String name);

}
