package com.sparta.server.threeserving.order.dto.response;

public record OrderItemOptionResponseDto(
        String optionName,
        Integer additionalPrice
) {
}
