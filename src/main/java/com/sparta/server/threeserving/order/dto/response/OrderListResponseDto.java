package com.sparta.server.threeserving.order.dto.response;

import com.sparta.server.threeserving.order.entity.Cart;
import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order.entity.Orders;

import java.time.Instant;
import java.util.UUID;

public record OrderListResponseDto(
        UUID id,
        OrderStatusEnum orderStatus,
        Integer totalPrice,
        Instant createdAt
) {
    public OrderListResponseDto(Orders order) {
        this(order.getId(), order.getOrderStatus(), order.getTotalPrice(), order.getCreatedAt());
    }
}