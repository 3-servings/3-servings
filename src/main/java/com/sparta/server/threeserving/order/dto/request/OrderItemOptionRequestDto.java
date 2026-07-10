package com.sparta.server.threeserving.order.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderItemOptionRequestDto(
        @NotNull UUID optionItemId,
        @NotNull String optionName,
        Integer additionalPrice
) {}
