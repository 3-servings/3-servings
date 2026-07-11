package com.sparta.server.threeserving.menu.repository;

import com.sparta.server.threeserving.menu.entity.OptionGroup;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OptionGroupRepository extends JpaRepository<OptionGroup, UUID> {

    boolean existsByStoreIdAndName(UUID storeId, String name);

    // 키워드 검색 및 페이징 적용
    Page<OptionGroup> findByStoreIdAndNameContainingIgnoreCase(UUID storeId, String name, Pageable pageable);

    // 키워드 없이 전체 조회 시 페이징 적용
    Page<OptionGroup> findAllByStoreId(UUID storeId, Pageable pageable);

    boolean existsByStoreIdAndNameAndIdNot(UUID storeId, String name, UUID optionGroupId);
}
