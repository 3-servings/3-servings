package com.sparta.server.threeserving.menu.repository;

import com.sparta.server.threeserving.menu.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, UUID> {

    // 특정 가게의 메뉴 카테고리 목록을 노출 순서(displayOrder) 오름차순 조회
    List<MenuCategory> findAllByStoreIdOrderByDisplayOrderAsc(UUID storeId);

    // 생성 시 중복 확인용
    boolean existsByStoreIdAndName(UUID storeId, String name);

    // 수정 시 중복 확인용
    boolean existsByStoreIdAndNameAndIdNot(UUID storeId, String name, UUID id);

    @Query("SELECT COALESCE(MAX(m.displayOrder), 0) FROM MenuCategory m WHERE m.store.id = :storeId")
    Integer findMaxDisplayOrder(@Param("storeId") UUID storeId);
}
