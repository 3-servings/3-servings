package com.sparta.server.threeserving.order.dto.response;

import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order.entity.Orders;

import java.util.UUID;

public record CheckoutResponseDto(
        UUID id,
        UUID cart_id,
        OrderStatusEnum orderStatus,
        String deliveryAddress,
        String requestMessage,
        UUID orderManagementId
) {
    public CheckoutResponseDto(Orders order, UUID orderManagementId, String deliveryAddress, String requestMessage) {
        this(order.getId(), order.getCart().getId(), order.getOrderStatus(), deliveryAddress, requestMessage, orderManagementId);
    }
}
