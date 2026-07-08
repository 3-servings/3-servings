package com.sparta.server.threeserving.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CartItemAddRequestDto(
        @NotNull UUID menuId,
        @Min(1) Integer quantity,
        List<UUID> optionItemIds
) {}

