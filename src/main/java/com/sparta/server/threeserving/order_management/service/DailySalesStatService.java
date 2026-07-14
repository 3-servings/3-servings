package com.sparta.server.threeserving.order_management.service;

import com.sparta.server.threeserving.order_management.dto.response.*;
import com.sparta.server.threeserving.order_management.entity.DailySalesStat;
import com.sparta.server.threeserving.order_management.repository.DailySalesStatRepository;
import com.sparta.server.threeserving.order_management.repository.OrderManagementRepository;
import com.sparta.server.threeserving.order_management.validator.StoreAccessValidator;
import com.sparta.server.threeserving.store.entity.Store;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class DailySalesStatService {

    private final OrderManagementRepository orderManagementRepository;
    private final DailySalesStatRepository dailySalesStatRepository;
    private final StoreAccessValidator storeAccessValidator;

    @Transactional
    public void createDailySalesStat(LocalDate yesterday) {

        OffsetDateTime start = yesterday.atStartOfDay().atOffset(ZoneOffset.of("+09:00"));

        OffsetDateTime end = yesterday.plusDays(1).atStartOfDay().atOffset(ZoneOffset.of("+09:00"));

        List<DailySalesStatResponse> results = orderManagementRepository.findDailySalesStats(start, end);


        List<DailySalesStat> stats = results.stream()
                .map(r -> DailySalesStat.builder()
                        .statDate(yesterday)
                        .store(Store.builder()
                                .id(r.getStoreId())
                                .build())
                        .totalOrderCount(r.getTotalOrderCount())
                        .completedCount(r.getCompletedCount())
                        .rejectedCount(r.getRejectedCount())
                        .canceledCount(r.getCanceledCount())
                        .totalSalesAmount(r.getTotalSalesAmount())
                        .avgCookTime(r.getAvgCookTime())
                        .completedRate(r.getCompletedRate())
                        .build())
                .toList();

        dailySalesStatRepository.saveAll(stats);


    }

    public DashboardTrendResponse getDashboardTrend(UUID storeId, LocalDate startDate, LocalDate endDate,Long userId, UserRoleEnum role) {

        if (role == UserRoleEnum.OWNER) {
            storeAccessValidator.validateStoreAccess(userId, storeId);
        }

        List<DashboardTrendResponse.Item> items = dailySalesStatRepository
                .findByStoreIdAndStatDateBetweenOrderByStatDate(
                        storeId,
                        startDate,
                        endDate
                )
                .stream()
                .map(stat -> DashboardTrendResponse.Item.builder()
                        .statDate(stat.getStatDate())
                        .totalOrderCount(stat.getTotalOrderCount())
                        .completedCount(stat.getCompletedCount())
                        .rejectedCount(stat.getRejectedCount())
                        .canceledCount(stat.getCanceledCount())
                        .totalSalesAmount(stat.getTotalSalesAmount())
                        .avgCookTime(stat.getAvgCookTime())
                        .acceptRate(stat.getCompletedRate())
                        .build())
                .toList();

        return DashboardTrendResponse.builder()
                .storeId(storeId)
                .items(items)
                .build();


    }

    public TodaySalesSummaryResponse getTodaySummary(UUID storeId,Long userId, UserRoleEnum role) {

        if (role == UserRoleEnum.OWNER) {
            storeAccessValidator.validateStoreAccess(userId, storeId);
        }
        return orderManagementRepository.findTodaySummary(storeId);

    }


    public RejectReasonStatResponse getRejectReasonStatistics(UUID storeId,Long userId, UserRoleEnum role) {

        if (role == UserRoleEnum.OWNER) {
            storeAccessValidator.validateStoreAccess(userId, storeId);
        }
        List<RejectReasonStatResponse.RejectReasonStatItem> items = orderManagementRepository.findRejectReasonStatistics(storeId)
                .stream()
                .map(row -> RejectReasonStatResponse.RejectReasonStatItem.builder()
                        .rejectReasonCode(row.getRejectReasonCode())
                        .description(row.getDescription())
                        .count(row.getCount())
                        .build())
                .toList();

        return RejectReasonStatResponse.builder()
                .storeId(storeId)
                .items(items)
                .build();
    }
}
