package com.sparta.server.threeserving.order_management.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class DashboardTrendResponse {

    private UUID storeId;
    private List<Item> items;

    @Getter
    @Builder
    public static class Item {

        private LocalDate statDate;
        private Long totalOrderCount;
        private Long completedCount;
        private Long rejectedCount;
        private Long canceledCount;
        private Long totalSalesAmount;
        private BigDecimal avgCookTime;
        private BigDecimal acceptRate;

    }
}
