package com.sparta.server.threeserving.order.dto.response;

import com.sparta.server.threeserving.order.dto.request.CartItemOptionRequestDto;

import java.util.List;
import java.util.UUID;

public record CartItemDetailResponseDto(
        UUID id,
        UUID menuId,
        String menuName,
        Integer quantity,
        List<CartItemOptionRequestDto> options
) {}
