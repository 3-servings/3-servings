package com.sparta.server.threeserving.order.dto.response;

import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order.entity.Orders;

import java.util.UUID;

public record OrderCancelResponseDto (
        UUID id,
        OrderStatusEnum orderStatus
) {
    public OrderCancelResponseDto(Orders order) {
        this(order.getId(), order.getOrderStatus());
    }
}
