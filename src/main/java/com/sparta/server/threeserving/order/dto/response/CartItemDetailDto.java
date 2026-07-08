package com.sparta.server.threeserving.order.dto.response;

import com.sparta.server.threeserving.order.dto.request.CartItemOptionDto;

import java.util.List;
import java.util.UUID;

public record CartItemDetailDto(
        UUID id,
        UUID menuId,
        String menuName,
        Integer quantity,
        List<CartItemOptionDto> options
) {}
