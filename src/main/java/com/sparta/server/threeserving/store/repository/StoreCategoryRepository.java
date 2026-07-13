package com.sparta.server.threeserving.store.repository;

import com.sparta.server.threeserving.store.entity.StoreCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreCategoryRepository extends JpaRepository<StoreCategory, UUID> {
}
