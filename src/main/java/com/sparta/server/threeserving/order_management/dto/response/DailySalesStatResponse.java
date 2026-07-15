package com.sparta.server.threeserving.order_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class DailySalesStatResponse {

    private UUID storeId;
    private Long totalOrderCount;
    private Long completedCount;
    private Long rejectedCount;
    private Long canceledCount;
    private Long totalSalesAmount;
    private Double avgCookTime;
    private Double completedRate;


}
