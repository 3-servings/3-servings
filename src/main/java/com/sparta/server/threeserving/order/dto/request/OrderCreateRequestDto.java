package com.sparta.server.threeserving.order.dto.request;

import com.sparta.server.threeserving.order.entity.Cart;
import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record OrderCreateRequestDto (
        @NotNull Long userId,
        @NotNull UUID storeId,
        Cart cart,
        @NotNull OrderStatusEnum orderStatus,
        @NotNull @Min(value = 0L) Integer totalPrice,
        @NotNull String deliveryAddress,
        String requestMessage,
        @NotNull List<OrderItemRequestDto> orderItems
) {}
