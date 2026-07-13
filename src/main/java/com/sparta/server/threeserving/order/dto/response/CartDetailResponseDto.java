package com.sparta.server.threeserving.order.dto.response;

import com.sparta.server.threeserving.order.entity.Cart;

import java.util.List;
import java.util.UUID;

public record CartDetailResponseDto(
    UUID id,
    UUID storeId,
    Integer estimatedTotalPrice,
    List<CartItemDetailResponseDto> items
) {
    public CartDetailResponseDto(Cart cart, Integer estimatedTotalPrice, List<CartItemDetailResponseDto> items) {
        this(cart.getId(), cart.getStoreId(), estimatedTotalPrice, items);
    }
}