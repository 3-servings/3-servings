package com.sparta.server.threeserving.order_management.controller;

import com.sparta.server.threeserving.auth.UserDetailsImpl;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order_management.dto.response.*;
import com.sparta.server.threeserving.order_management.dto.request.OrderAcceptRequest;
import com.sparta.server.threeserving.order_management.dto.request.OrderRejectRequest;
import com.sparta.server.threeserving.order_management.dto.request.OrderStatusUpdateRequest;
import com.sparta.server.threeserving.order_management.dto.request.UpdateCookingTimeRequest;
import com.sparta.server.threeserving.order_management.service.DailySalesStatService;
import com.sparta.server.threeserving.order_management.service.OrderManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

import static com.sparta.server.threeserving.global.common.response.SuccessCode.SUCCESS;

@RestController
@RequestMapping("/api/order-management/")
@RequiredArgsConstructor
public class OrderManagementController {
    private final OrderManagementService orderManagementService;
    private final DailySalesStatService dailySalesStatService;

    @GetMapping("/stores/{storeId}/orders")
    public ApiResponse<Page<OrderManagementListResponse>> getOrderManagementList(
            @PathVariable UUID storeId,
            @RequestParam(required = false) OrderStatusEnum status,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Page<OrderManagementListResponse> response = orderManagementService.getOrderManagementList(
                storeId,
                status,
                pageable,
                userDetails.getUser().getId(),
                userDetails.getUser().getRole());

        return ApiResponse.success(SUCCESS, response);
    }

    @GetMapping("/orders/{orderManagementId}")
    public ApiResponse<OrderManagementResponse> getOrderManagementDetail(@PathVariable UUID orderManagementId) {
        OrderManagementResponse response = orderManagementService.getOrderManagementDetail(orderManagementId);

        return ApiResponse.success(SUCCESS, response);
    }

    @PatchMapping("/orders/{orderManagementId}/accept")
    public ApiResponse<Void> acceptOrder(
            @PathVariable UUID orderManagementId,
            @RequestBody @Valid OrderAcceptRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        orderManagementService.acceptOrder(
                orderManagementId,
                request.getEstimatedCookTime(),
                userDetails.getUser().getId(),
                userDetails.getUser().getRole());

        return ApiResponse.success(SUCCESS, null);
    }

    @PatchMapping("/orders/{orderManagementId}/reject")
    public ApiResponse<Void> rejectOrder(
            @PathVariable UUID orderManagementId,
            @RequestBody @Valid OrderRejectRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        orderManagementService.rejectOrder(
                orderManagementId,
                request.getRejectReasonCodeId(),
                request.getMemo(),
                userDetails.getUser().getId(),
                userDetails.getUser().getRole());

        return ApiResponse.success(SUCCESS, null);
    }

    @PatchMapping("/orders/{orderManagementId}/status")
    public ApiResponse<Void> updateStatus(
            @PathVariable UUID orderManagementId,
            @RequestBody @Valid OrderStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        orderManagementService.updateStatus(
                orderManagementId,
                request.getStatus(),
                userDetails.getUser().getId(),
                userDetails.getUser().getRole());

        return ApiResponse.success(SUCCESS, null);
    }


    @PatchMapping("/orders/{orderManagementId}/cook-time")
    public ApiResponse<Void> updateCookingTime(
            @PathVariable UUID orderManagementId,
            @RequestBody @Valid UpdateCookingTimeRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        orderManagementService.updateCookingTime(
                orderManagementId,
                request.getEstimatedCookTime(),
                userDetails.getUser().getId(),
                userDetails.getUser().getRole());

        return ApiResponse.success(SUCCESS, null);
    }

    @GetMapping("/orders/{orderManagementId}/history")
    public ApiResponse<OrderStatusHistoryResponse> getOrderStatusHistory(@PathVariable UUID orderManagementId) {
        OrderStatusHistoryResponse response = orderManagementService.getOrderStatusHistory(orderManagementId);

        return ApiResponse.success(SUCCESS, response);
    }

    @GetMapping("/stores/{storeId}/dashboard/summary")
    public ApiResponse<TodaySalesSummaryResponse> getDashboardSummary(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        TodaySalesSummaryResponse response = dailySalesStatService.getTodaySummary(
                storeId,
                userDetails.getUser().getId(),
                userDetails.getUser().getRole());
        return ApiResponse.success(SUCCESS, response);
    }

    @GetMapping("/stores/{storeId}/dashboard/trend")
    public ApiResponse<DashboardTrendResponse> getDashboardTrend(
            @PathVariable UUID storeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        DashboardTrendResponse response = dailySalesStatService.getDashboardTrend(
                storeId,
                startDate,
                endDate,
                userDetails.getUser().getId(),
                userDetails.getUser().getRole());
        return ApiResponse.success(SUCCESS, response);
    }

    @GetMapping("/stores/{storeId}/dashboard/reject-reasons")
    public ApiResponse<RejectReasonStatResponse> getRejectReasonStatistics(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        RejectReasonStatResponse response = dailySalesStatService.getRejectReasonStatistics(
                storeId,
                userDetails.getUser().getId(),
                userDetails.getUser().getRole());
        return ApiResponse.success(SUCCESS, response);
    }
}
