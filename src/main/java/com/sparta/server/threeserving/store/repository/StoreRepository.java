package com.sparta.server.threeserving.store.repository;

import com.sparta.server.threeserving.store.entity.Store;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.sparta.server.threeserving.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {
    @Query("SELECT DISTINCT s FROM Store s " +
            "LEFT JOIN s.categoryList sc " +
            "LEFT JOIN Menu m ON m.store = s " +
            "WHERE (:name IS NULL OR s.name LIKE CONCAT('%', :name, '%') OR m.name LIKE CONCAT('%', :name, '%'))  " +
            "AND (:regionId IS NULL OR s.region.id = :regionId) " +
            "AND (:categoryId IS NULL OR sc.category.id = :categoryId)")
    Page<Store> searchStores(
            @Param("name") String name,
            @Param("regionId") UUID regionId,
            @Param("categoryId") UUID categoryId,
            Pageable pageable
    );
    List<UUID> findStoreIdsByOwnerId(Long ownerId);
}
