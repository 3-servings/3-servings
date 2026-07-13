package com.sparta.server.threeserving.order.dto.response;

import com.sparta.server.threeserving.order.entity.OrderItem;

import java.util.List;

public record OrderItemResponseDto(
        String menuName,
        Integer price,
        Integer quantity,
        List<OrderItemOptionResponseDto> options
) {
    public OrderItemResponseDto(OrderItem orderItem, List<OrderItemOptionResponseDto> options) {
        this(orderItem.getMenuName(), orderItem.getPrice(), orderItem.getQuantity(), options);
    }
}
