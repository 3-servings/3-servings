package com.sparta.server.threeserving.order.repository;

import com.sparta.server.threeserving.order.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUserIdAndStoreIdAndDeletedAtIsNull(Long userId, UUID storeId);

    List<Cart> findAllByUserIdAndDeletedAtIsNull(Long userId);

    Optional<Cart> findByIdAndDeletedAtIsNull(UUID id);
}
