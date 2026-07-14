package com.sparta.server.threeserving.order.dto.response;

import com.sparta.server.threeserving.order.entity.Orders;

import java.util.UUID;

public record OrderModifyResponseDto (
        UUID id,
        String deliveryAddress,
        String requestMessage
) {
    public OrderModifyResponseDto(Orders order) {
        this(order.getId(), order.getDeliveryAddress(), order.getRequestMessage());
    }
}
