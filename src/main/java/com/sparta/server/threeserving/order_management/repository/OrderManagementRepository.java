package com.sparta.server.threeserving.order_management.repository;


import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order.entity.Orders;
import com.sparta.server.threeserving.order_management.dto.response.DailySalesStatResponse;
import com.sparta.server.threeserving.order_management.dto.response.RejectReasonStatResponse;
import com.sparta.server.threeserving.order_management.dto.response.TodaySalesSummaryResponse;
import com.sparta.server.threeserving.order_management.entity.OrderManagement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderManagementRepository extends JpaRepository<OrderManagement, UUID>, OrderManagementRepositoryCustom {
    Page<OrderManagement>  findByStoreId(UUID storeId, Pageable pageable);

    Page<OrderManagement>  findByStoreIdAndOrderStatus(UUID storeId, OrderStatusEnum orderStatus, Pageable pageable);

    List<DailySalesStatResponse> findDailySalesStats(Instant start,Instant end);

    TodaySalesSummaryResponse findTodaySummary(UUID storeId);

    List<RejectReasonStatResponse.RejectReasonStatItem> findRejectReasonStatistics(UUID storeId);

    Optional<OrderManagement> findByOrders(Orders orders);

}


