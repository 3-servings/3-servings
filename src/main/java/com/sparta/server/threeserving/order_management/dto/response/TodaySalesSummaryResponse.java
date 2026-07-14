package com.sparta.server.threeserving.order_management.dto.response;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class TodaySalesSummaryResponse {

    private UUID storeId;

    private Long totalOrderCount;
    private Long completedCount;
    private Long rejectedCount;
    private Long canceledCount;

    private Long totalSalesAmount;

    private BigDecimal avgCookTime;

    private BigDecimal completedRate;

    public TodaySalesSummaryResponse(
            UUID storeId,
            Long totalOrderCount,
            Long completedCount,
            Long rejectedCount,
            Long canceledCount,
            Long totalSalesAmount,
            BigDecimal avgCookTime,
            BigDecimal completedRate
    ) {
        this.storeId = storeId;
        this.totalOrderCount = totalOrderCount;
        this.completedCount = completedCount;
        this.rejectedCount = rejectedCount;
        this.canceledCount = canceledCount;
        this.totalSalesAmount = totalSalesAmount;
        this.avgCookTime = avgCookTime;
        this.completedRate = completedRate;
    }
}

