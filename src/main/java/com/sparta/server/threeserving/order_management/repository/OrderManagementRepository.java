package com.sparta.server.threeserving.order_management.repository;


import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order.entity.Orders;
import com.sparta.server.threeserving.order_management.dto.response.DailySalesStatResponse;
import com.sparta.server.threeserving.order_management.dto.response.TodaySalesSummaryResponse;
import com.sparta.server.threeserving.order_management.entity.OrderManagement;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderManagementRepository extends JpaRepository<OrderManagement, UUID> {
    Page<OrderManagement>  findByStoreId(UUID storeId, Pageable pageable);

    Page<OrderManagement>  findByStoreIdAndOrderStatus(UUID storeId, OrderStatusEnum orderStatus, Pageable pageable);

//ToDo : QueryDSL로 변경

    @Query(value = """
        SELECT
            om.store_id AS storeId,
            COUNT(*) AS totalOrderCount,
            SUM(CASE WHEN om.order_status = 'COMPLETED' THEN 1 ELSE 0 END) AS completedCount,
            SUM(CASE WHEN om.order_status = 'REJECTED' THEN 1 ELSE 0 END) AS rejectedCount,
            SUM(CASE WHEN om.order_status = 'CANCELED' THEN 1 ELSE 0 END) AS canceledCount,
            SUM(o.total_price) AS totalSalesAmount,
            AVG(om.estimated_cook_time) AS avgCookTime,
            (SUM(CASE WHEN om.order_status = 'COMPLETED' THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) AS completedRate
        FROM p_order_management om
        JOIN p_order o ON om.order_id = o.id
        WHERE om.created_at BETWEEN :start AND :end
        GROUP BY om.store_id
        """, nativeQuery = true)
    List<DailySalesStatResponse> findDailySalesStats(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    @Query(value = """
        SELECT
            om.store_id AS storeId,
            COUNT(*) AS totalOrderCount,
            SUM(CASE WHEN om.order_status = 'COMPLETED' THEN 1 ELSE 0 END) AS completedCount,
            SUM(CASE WHEN om.order_status = 'REJECTED' THEN 1 ELSE 0 END) AS rejectedCount,
            SUM(CASE WHEN om.order_status = 'CANCELED' THEN 1 ELSE 0 END) AS canceledCount,
            SUM(o.total_price) AS totalSalesAmount,
            AVG(om.estimated_cook_time) AS avgCookTime,
            (SUM(CASE WHEN om.order_status = 'COMPLETED' THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) AS completedRate
        FROM p_order_management om
        JOIN p_order o ON om.order_id = o.id
        WHERE om.store_id = :storeId
        AND DATE(om.created_at) = CURRENT_DATE
        GROUP BY om.store_id
        """, nativeQuery = true)
    TodaySalesSummaryResponse findTodaySummary(
            @Param("storeId") UUID storeId);


    @Query(value = """
        SELECT
            rr.code AS rejectReasonCode,
            rr.description AS description,
            COUNT(*) AS count
        FROM p_order_management om
        JOIN p_reject_reason_code rr
            ON om.reject_reason_id = rr.id
        WHERE om.store_id = :storeId
          AND om.order_status = 'REJECTED'
        GROUP BY
            rr.code,
            rr.description
        ORDER BY
            count DESC
        """, nativeQuery = true)
    List<RejectReasonStatRow> findRejectReasonStatistics(
            @Param("storeId") UUID storeId
    );

    Optional<OrderManagement> findByOrders(Orders orders);

    interface RejectReasonStatRow {
        String getRejectReasonCode();
        String getDescription();
        Long getCount();
    }

}


