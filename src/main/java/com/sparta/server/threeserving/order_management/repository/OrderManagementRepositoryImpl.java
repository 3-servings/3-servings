package com.sparta.server.threeserving.order_management.repository;


import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order.entity.QOrders;
import com.sparta.server.threeserving.order_management.dto.response.DailySalesStatResponse;
import com.sparta.server.threeserving.order_management.dto.response.RejectReasonStatResponse;
import com.sparta.server.threeserving.order_management.dto.response.TodaySalesSummaryResponse;
import com.sparta.server.threeserving.order_management.entity.QOrderManagement;
import com.sparta.server.threeserving.order_management.entity.QRejectReasonCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;


@RequiredArgsConstructor
@Repository
public class OrderManagementRepositoryImpl implements OrderManagementRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public List<DailySalesStatResponse> findDailySalesStats(Instant start, Instant end) {
        QOrderManagement om = QOrderManagement.orderManagement;
        QOrders o = QOrders.orders;
        return queryFactory
                .select(Projections.constructor(
                        DailySalesStatResponse.class,
                        om.store.id,
                        om.count(),
                        new CaseBuilder()
                                .when(om.orderStatus.eq(OrderStatusEnum.COMPLETED))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        new CaseBuilder()
                                .when(om.orderStatus.eq(OrderStatusEnum.REJECTED))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        new CaseBuilder()
                                .when(om.orderStatus.eq(OrderStatusEnum.CANCELED))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        o.totalPrice.sum().castToNum(Long.class),
                        om.estimatedCookTime.avg(),
                        new CaseBuilder()
                                .when(om.orderStatus.eq(OrderStatusEnum.COMPLETED))
                                .then(1.0)
                                .otherwise(0.0)
                                .sum()
                                .multiply(100.0)
                                .divide(om.count())

                ))
                .from(om)
                .join(o)
                .on(om.orders.id.eq(o.id))
                .where(
                        om.createdAt.between(start,end)
                )
                .groupBy(om.store.id)
                .fetch();
    }

    @Override
    public TodaySalesSummaryResponse findTodaySummary(UUID storeId) {
        QOrderManagement om = QOrderManagement.orderManagement;
        QOrders o = QOrders.orders;

        LocalDate today = LocalDate.now();

        ZoneId KST = ZoneId.of("Asia/Seoul");

        Instant start = today
                .atStartOfDay(KST)
                .toInstant();

        Instant end = today
                .plusDays(1)
                .atStartOfDay(KST)
                .toInstant();


        return queryFactory
                .select(Projections.constructor(
                        TodaySalesSummaryResponse.class,
                        om.store.id,
                        om.count(),
                        new CaseBuilder()
                                .when(om.orderStatus.eq(OrderStatusEnum.PENDING))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        new CaseBuilder()
                                .when(om.orderStatus.eq(OrderStatusEnum.ACCEPTED))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        new CaseBuilder()
                                .when(om.orderStatus.eq(OrderStatusEnum.COOKING))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        new CaseBuilder()
                                .when(om.orderStatus.eq(OrderStatusEnum.READY))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        new CaseBuilder()
                                .when(om.orderStatus.eq(OrderStatusEnum.COMPLETED))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        new CaseBuilder()
                                .when(om.orderStatus.eq(OrderStatusEnum.REJECTED))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        new CaseBuilder()
                                .when(om.orderStatus.eq(OrderStatusEnum.CANCELED))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        o.totalPrice.sum().castToNum(Long.class),
                        om.estimatedCookTime.avg(),
                        new CaseBuilder()
                                .when(om.orderStatus.eq(OrderStatusEnum.COMPLETED))
                                .then(1.0)
                                .otherwise(0.0)
                                .sum()
                                .multiply(100.0)
                                .divide(om.count())
                ))
                .from(om)
                .join(o)
                .on(om.orders.id.eq(o.id))
                .where(
                        om.store.id.eq(storeId),
                        om.createdAt.between(start,end)
                )
                .groupBy(om.store.id)
                .fetchOne();
    }

    @Override
    public List<RejectReasonStatResponse.RejectReasonStatItem> findRejectReasonStatistics(UUID storeId) {
        QOrderManagement om = QOrderManagement.orderManagement;
        QRejectReasonCode rr = QRejectReasonCode.rejectReasonCode;
        return queryFactory
                .select(Projections.constructor(
                        RejectReasonStatResponse.RejectReasonStatItem.class,
                        rr.code,
                        rr.description,
                        om.count()
                ))
                .from(om)
                .join(om.rejectReasonCode, rr)
                .where(
                        om.store.id.eq(storeId),
                        om.orderStatus.eq(OrderStatusEnum.REJECTED)
                )
                .groupBy(
                        rr.code,
                        rr.description
                )
                .orderBy(
                        om.count().desc()
                )
                .fetch();
    }


}
