package com.sparta.server.threeserving.store.repository;

import com.sparta.server.threeserving.store.dto.response.CategoryResponse;
import com.sparta.server.threeserving.store.entity.Category;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByName(@NotNull String name);

    Page<Category> findByIsActive(boolean isActive, Pageable newPageable);

    @Query("SELECT c from  Category  c " +
            "WHERE c.isActive = :isActive " +
            "AND (:name IS NULL OR c.name LIKE CONCAT('%', :name , '%') )")
    Page<Category> searchCategories(
            @Param("isActive") Boolean isActive,
            @Param("name") String name,
            Pageable pageable
    );
}
