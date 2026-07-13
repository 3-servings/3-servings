package com.sparta.server.threeserving.order_management.dto.response;

import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order_management.entity.OrderManagement;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class OrderManagementResponse {

        private UUID orderId;
        private UUID storeId;
        private UUID orderManagementId;
        private OrderStatusEnum orderStatus;
        private Integer estimatedCookTime;
        private String rejectMemo;
        private OffsetDateTime acceptedAt;
        private OffsetDateTime cookingStartedAt;
        private OffsetDateTime readyAt;
        private OffsetDateTime completedAt;
        private OffsetDateTime rejectedAt;

    public OrderManagementResponse(OrderManagement orderManagement) {

            this.orderId = orderManagement.getOrders().getId();
            this.storeId = orderManagement.getStoreId();
            this.orderManagementId = orderManagement.getId();
            this.orderStatus = orderManagement.getOrderStatus();
            this.estimatedCookTime = orderManagement.getEstimatedCookTime();
            this.rejectMemo = orderManagement.getRejectMemo();
            this.acceptedAt = orderManagement.getAcceptedAt();
            this.cookingStartedAt = orderManagement.getCookingStartedAt();
            this.readyAt = orderManagement.getReadyAt();
            this.completedAt = orderManagement.getCompletedAt();
            this.rejectedAt = orderManagement.getRejectedAt();
        }

}
