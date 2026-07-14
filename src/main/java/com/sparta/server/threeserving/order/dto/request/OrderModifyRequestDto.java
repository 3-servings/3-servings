package com.sparta.server.threeserving.order.dto.request;

import jakarta.validation.constraints.NotNull;

public record OrderModifyRequestDto(
        @NotNull String deliveryAddress,
        String requestMessage
) {}
