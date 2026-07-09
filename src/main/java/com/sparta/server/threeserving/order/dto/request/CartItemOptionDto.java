package com.sparta.server.threeserving.order.dto.request;

import java.util.UUID;

public record CartItemOptionDto(
        UUID optionItemId,
        String optionName
) {}
