package com.sparta.server.threeserving.menu.repository;

import com.sparta.server.threeserving.menu.entity.Menu;
import com.sparta.server.threeserving.menu.enums.MenuStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID> {

    // 생성 시 중복 확인용
    boolean existsByStoreIdAndName(UUID storeId, String name);

    // 수정 시 중복 확인용
    boolean existsByStoreIdAndNameAndIdNot(UUID storeId, String name, UUID menuId);

    boolean existsByMenuCategoryIdAndDeletedAtIsNull(UUID menuCategoryId);

    // 특정 카테고리 내에서의 displayOrder 최대값 조회
    @Query("SELECT COALESCE(MAX(m.displayOrder), 0) FROM Menu m WHERE m.menuCategory.id = :menuCategoryId AND m.deletedAt IS NULL")
    int findMaxDisplayOrderByMenuCategoryId(@Param("menuCategoryId") UUID menuCategoryId);

    // status, 키워드 검색
    @Query("SELECT m FROM Menu m WHERE m.store.id = :storeId AND (:status IS NULL OR m.status = :status) AND (:keyword IS NULL OR m.name LIKE %:keyword%)")
    Page<Menu> findMenusByCondition(
            @Param("storeId") UUID storeId,
            @Param("status") MenuStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 고객용 메뉴판 조회, 판매중/품절 메뉴만 조회하며 카테고리와 함께 Fetch Join
    @Query("SELECT m FROM Menu m " +
            "JOIN FETCH m.menuCategory mc " +
            "WHERE m.store.id = :storeId " +
            "AND m.status IN :statuses " +
            "AND mc.deletedAt IS NULL " +
            "ORDER BY mc.displayOrder ASC, m.displayOrder ASC")
    List<Menu> findMenusWithCategory(
            @Param("storeId") UUID storeId,
            @Param("statuses") List<MenuStatus> statuses
    );

    // 메뉴 상세 조회용 쿼리 Fetch Join
    @Query("SELECT m FROM Menu m " +
            "JOIN FETCH m.menuCategory " +
            "LEFT JOIN FETCH m.menuOptionGroups mog " +
            "LEFT JOIN FETCH mog.optionGroup " +
            "WHERE m.id = :menuId")
    Optional<Menu> findMenuDetailById(@Param("menuId") UUID menuId);

    @EntityGraph(attributePaths = {"store", "store.owner"})
    @Query("SELECT m FROM Menu m WHERE m.id = :id")
    Optional<Menu> findByIdWithStoreAndOwner(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"store"})
    @Query("SELECT m FROM Menu m WHERE m.id IN :ids")
    List<Menu> findAllByIdInWithStore(@Param("ids") Iterable<UUID> ids);

    @EntityGraph(attributePaths = {"store", "menuCategory"})
    @Query("SELECT m FROM Menu m WHERE m.id IN :ids")
    List<Menu> findAllByIdInWithStoreAndCategory(@Param("ids") Iterable<UUID> ids);
}
