package com.sparta.server.threeserving.store.repository;

import com.sparta.server.threeserving.store.entity.Store;
import com.sparta.server.threeserving.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {
    List<UUID> findStoreIdsByOwnerId(Long ownerId);
}
