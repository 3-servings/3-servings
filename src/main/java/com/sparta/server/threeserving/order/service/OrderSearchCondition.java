package com.sparta.server.threeserving.order.service;

import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record OrderSearchCondition (
        UUID storeId,
        Long userId,
        OrderStatusEnum orderStatusEnum,
        int size,
        int page,
        String sortBy,
        boolean isAsc
)
{
    private static final List<Integer> ALLOWED_SIZE = List.of(10, 30, 50);
    public int sanitizedSize() {
        return ALLOWED_SIZE.contains(size) ? size : 10;
    }
}
