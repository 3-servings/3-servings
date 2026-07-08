package com.sparta.server.threeserving.order.dto.response;

import com.sparta.server.threeserving.order.entity.Cart;

import java.time.LocalDateTime;
import java.util.UUID;

public record CartListResponseDto(
        UUID id,
        UUID storeId,
        String storeName,
        Long quantity,
        LocalDateTime createdAt
) {
    public CartListResponseDto(Cart cart, String storeName, Long quantity) {
        this(cart.getId(), cart.getStoreId(), storeName, quantity, cart.getCreatedAt());
    }
}