package com.sparta.server.threeserving.order.dto.response;

import java.util.UUID;

public record CartAddItemResponseDto (
        UUID id,
        UUID cartId,
        UUID menuId,
        Integer quantity
) {}