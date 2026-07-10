package com.sparta.server.threeserving.order_management.dto;

import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order_management.entity.OrderManagement;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class OrderManagementListResponse {

    private UUID orderManagementId;
    private UUID orderId;
    private UUID storeId;
    private OrderStatusEnum orderStatusEnum;
    private LocalDateTime orderedAt;

    public OrderManagementListResponse(OrderManagement orderManagement) {
        this.orderManagementId = orderManagement.getId();
//        this.orderId = orderManagement.getOrder().getId();
//        this.storeId = orderManagement.getStore().getId();
        this.orderStatusEnum = orderManagement.getOrderStatus();
    }
}
