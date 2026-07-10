package com.sparta.server.threeserving.menu.repository;

import com.sparta.server.threeserving.menu.entity.MenuOptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MenuOptionGroupRepository extends JpaRepository<MenuOptionGroup, UUID> {

    // 특정 메뉴의 매핑 삭제용
    void deleteByMenuId(UUID menuId);
}
