package com.sparta.server.threeserving.order.repository;

import com.sparta.server.threeserving.order.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    List<CartItem> findAllByCart_IdAndDeletedAtIsNull(UUID cartId);

    Optional<CartItem> findByIdAndCart_IdAndDeletedAtIsNull(UUID id, UUID cartId);

    interface CartItemCountProjection {
        UUID getCartId();
        Long getCount();
    }

    @Query("SELECT ci.cart.id AS cartId, COUNT(ci) AS count " +
            "FROM CartItem ci WHERE ci.cart.id IN :cartIds AND ci.deletedAt IS NULL " +
            "GROUP BY ci.cart.id")
    List<CartItemCountProjection> countByCartIdIn(@Param("cartIds") List<UUID> cartIds);

}
