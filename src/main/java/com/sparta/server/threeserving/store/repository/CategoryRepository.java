package com.sparta.server.threeserving.store.repository;

import com.sparta.server.threeserving.store.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}
