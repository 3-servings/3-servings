package com.sparta.server.threeserving.store.repository;

import com.sparta.server.threeserving.store.dto.response.CategoryResponse;
import com.sparta.server.threeserving.store.entity.Category;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByName(@NotNull String name);

    Page<Category> findByIsActive(boolean isActive, Pageable newPageable);
}
