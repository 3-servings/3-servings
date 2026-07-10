package com.sparta.server.threeserving.order.service;

import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
public class OrderSearchCondition
{
    private UUID storeId;
    private Long userId;
    private OrderStatusEnum orderStatusEnum;
    private int size;
    private int page;
    private String sortBy;
    private boolean isAsc;

    private static final List<Integer> ALLOWED_SIZE = List.of(10, 30, 50);
    public int sanitizedSize() {
        return ALLOWED_SIZE.contains(size) ? size : 10;
    }

    public OrderSearchCondition(UUID storeId, Long userId, OrderStatusEnum orderStatusEnum, int size, int page, String sortBy, boolean isAsc) {
        this.storeId = storeId;
        this.userId = userId;
        this.orderStatusEnum = orderStatusEnum;
        this.size = size;
        this.page = page;
        this.sortBy = sortBy;
        this.isAsc = isAsc;
    }
}
