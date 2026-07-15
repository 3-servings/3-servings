package com.sparta.server.threeserving.order_management.repository;

import com.sparta.server.threeserving.order_management.dto.response.DailySalesStatResponse;
import com.sparta.server.threeserving.order_management.dto.response.RejectReasonStatResponse;
import com.sparta.server.threeserving.order_management.dto.response.TodaySalesSummaryResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OrderManagementRepositoryCustom {
    List<DailySalesStatResponse> findDailySalesStats(Instant start, Instant end);
    TodaySalesSummaryResponse findTodaySummary(UUID storeId);
    List<RejectReasonStatResponse.RejectReasonStatItem> findRejectReasonStatistics(UUID storeId);
}
