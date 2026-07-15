package com.sparta.server.threeserving.order.repository;

import com.sparta.server.threeserving.order.entity.CartItem;
import com.sparta.server.threeserving.order.entity.CartItemOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CartItemOptionRepository extends JpaRepository<CartItemOption, UUID> {
    List<CartItemOption> findAllByCartItemInAndDeletedAtIsNull(List<CartItem> cartItems);

    List<CartItemOption> findAllByCartItem(CartItem cartItem);

    List<CartItemOption> findAllByCartItemIn(List<CartItem> cartItems);
}
