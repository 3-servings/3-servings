package com.sparta.server.threeserving.order.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record OrderItemRequestDto (
        @NotNull UUID menuId,
        @NotNull String menuName,
        @NotNull Integer price,
        @NotNull Integer quantity,
        @NotNull List<OrderItemOptionRequestDto> options
) {}
