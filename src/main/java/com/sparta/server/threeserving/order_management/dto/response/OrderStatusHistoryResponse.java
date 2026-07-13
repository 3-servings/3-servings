package com.sparta.server.threeserving.order_management.dto.response;

import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class OrderStatusHistoryResponse{
        private UUID orderManagementId;
        private List<History> history;

        @Getter
        @AllArgsConstructor
        public static class History {

    private OrderStatusEnum previousStatus;
    private OrderStatusEnum currentStatus;
    private String memo;
    private Instant changedAt; // BaseEntity의 createdAt 타입에 맞추세요.
}
}
