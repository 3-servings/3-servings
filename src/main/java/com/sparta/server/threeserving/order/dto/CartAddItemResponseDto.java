package com.sparta.server.threeserving.order.dto;

import com.sparta.server.threeserving.order.entity.Cart;

import java.util.UUID;

public record CartAddItemResponseDto (
        UUID id,
        Cart cart,
        UUID menuId,
        Integer quantity
) {}