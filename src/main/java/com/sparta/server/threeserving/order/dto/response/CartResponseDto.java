package com.sparta.server.threeserving.order.dto.response;

import com.sparta.server.threeserving.order.entity.Cart;

import java.time.LocalDateTime;
import java.util.UUID;

public record CartResponseDto (
        UUID id,
        Long userId,
        UUID storeId,
        LocalDateTime createdAt
) {
    public CartResponseDto(Cart cart) {
        this(cart.getId(), cart.getUserId(), cart.getStoreId(), cart.getCreatedAt());
    }
}