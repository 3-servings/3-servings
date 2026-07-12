package com.sparta.server.threeserving.menu.repository;

import com.sparta.server.threeserving.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID> {

    boolean existsByStoreIdAndName(UUID storeId, String name);

    // 특정 카테고리 내에서의 displayOrder 최대값 조회
    @Query("SELECT COALESCE(MAX(m.displayOrder), 0) FROM Menu m WHERE m.menuCategory.id = :menuCategoryId")
    int findMaxDisplayOrderByMenuCategoryId(@Param("menuCategoryId") UUID menuCategoryId);

}
