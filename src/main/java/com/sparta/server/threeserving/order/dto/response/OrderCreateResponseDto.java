package com.sparta.server.threeserving.order.dto.response;

import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order.entity.Orders;

import java.time.Instant;
import java.util.UUID;

public record OrderCreateResponseDto(
    UUID id,
    UUID cartId,
    UUID storeId,
    Long userId,
    OrderStatusEnum orderStatus,
    Integer totalPrice,
    String deliveryAddress,
    Instant created_at,
    Long created_by,
    UUID orderManagementId
) {
    public OrderCreateResponseDto(Orders orders, UUID orderManagementId){
        this(
                orders.getId(),
                (orders.getCart() == null) ? null : orders.getCart().getId(),
                orders.getStoreId(), orders.getUserId(),
                orders.getOrderStatus(), orders.getTotalPrice(), orders.getDeliveryAddress(), orders.getCreatedAt(), orders.getCreatedBy(),
                orderManagementId);
    }
}