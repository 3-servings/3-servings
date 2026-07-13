package com.sparta.server.threeserving.order_management.service;

import com.sparta.server.threeserving.order_management.dto.response.DailySalesStatResponse;
import com.sparta.server.threeserving.order_management.entity.DailySalesStat;
import com.sparta.server.threeserving.order_management.repository.DailySalesStatRepository;
import com.sparta.server.threeserving.order_management.repository.OrderManagementRepository;
import com.sparta.server.threeserving.store.entity.Store;
import com.sparta.server.threeserving.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class DailySalesStatService {

    private final OrderManagementRepository orderManagementRepository;
    private final DailySalesStatRepository dailySalesStatRepository;
    private  final StoreRepository storeRepository;

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
}
