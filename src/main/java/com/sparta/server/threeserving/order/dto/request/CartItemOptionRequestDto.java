package com.sparta.server.threeserving.order.dto.request;

import java.util.UUID;

public record CartItemOptionRequestDto(
        UUID optionItemId,
        String optionName
) {}
