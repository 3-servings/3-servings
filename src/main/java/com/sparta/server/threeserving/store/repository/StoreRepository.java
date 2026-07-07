package com.sparta.server.threeserving.store.repository;

import com.sparta.server.threeserving.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {
}
