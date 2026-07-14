package com.sparta.server.threeserving.store.repository;

import com.sparta.server.threeserving.store.entity.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RegionRepository extends JpaRepository<Region, UUID> {
    boolean existsByName(String name);

    Page<Region> findByIsServiceArea(boolean isServiceArea, Pageable newPageable);
}
