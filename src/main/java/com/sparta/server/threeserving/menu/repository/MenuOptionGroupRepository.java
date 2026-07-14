package com.sparta.server.threeserving.menu.repository;

import com.sparta.server.threeserving.menu.entity.MenuOptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MenuOptionGroupRepository extends JpaRepository<MenuOptionGroup, UUID> {

    // 특정 메뉴의 매핑 삭제용
    void deleteByMenuId(UUID menuId);

    // 특정 메뉴에 연결된 옵션 그룹 목록 조회 (장바구니 담기 시 선택 개수 검증용)
    List<MenuOptionGroup> findAllByMenuId(UUID menuId);
}
