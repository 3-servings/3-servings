package com.sparta.server.threeserving.order.dto.request;

public record OrderModifyRequestDto(
        String deliveryAddress,
        String requestMessage
) {}
