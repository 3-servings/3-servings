package com.sparta.server.threeserving.order.dto.response;

import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order.entity.Orders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderDetailResponseDto(
    UUID id,
    UUID storeId,
    OrderStatusEnum orderStatus,
    Integer totalPrice,
    String deliveryAddress,
    List<OrderItemResponseDto> items,
    Instant created_at
) {
    public OrderDetailResponseDto(Orders order, List<OrderItemResponseDto> items){
        this(order.getId(), order.getStoreId(),
                order.getOrderStatus(), order.getTotalPrice(), order.getDeliveryAddress(), items, order.getCreatedAt());
    }
}