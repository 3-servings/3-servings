package com.sparta.server.threeserving.order_management.controller;

import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order_management.dto.request.OrderAcceptRequest;
import com.sparta.server.threeserving.order_management.dto.request.OrderRejectRequest;
import com.sparta.server.threeserving.order_management.dto.request.OrderStatusUpdateRequest;
import com.sparta.server.threeserving.order_management.dto.response.OrderManagementListResponse;
import com.sparta.server.threeserving.order_management.dto.response.OrderManagementResponse;
import com.sparta.server.threeserving.order_management.service.OrderManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.sparta.server.threeserving.global.common.response.SuccessCode.SUCCESS;

@RestController
@RequestMapping("/api/order-management/")
@RequiredArgsConstructor
public class OrderManagementController {
    private final OrderManagementService orderManagementService;

    //신규/전체 주문 목록 조회
    @GetMapping("/stores/{storeId}/orders")
    public ApiResponse<Page<OrderManagementListResponse>> getOrderManagementList(
            @PathVariable UUID storeId,
            @RequestParam(required = false) OrderStatusEnum status,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<OrderManagementListResponse> response = orderManagementService.getOrderManagementList(storeId, status, pageable);

         return ApiResponse.success(SUCCESS,response);
    }


    //주문 상세 조회
    @GetMapping("/orders/{orderManagementId}")
    public ApiResponse<OrderManagementResponse> getOrderManagementDetail(
            @PathVariable UUID orderManagementId
    ) {
        OrderManagementResponse response = orderManagementService.getOrderManagementDetail(orderManagementId);

        return ApiResponse.success(SUCCESS,response);
    }

    @PatchMapping("/{orderManagementId}/accept")
    public ApiResponse<Void> acceptOrder( @PathVariable UUID orderManagementId,@RequestBody @Valid OrderAcceptRequest request) {

        orderManagementService.acceptOrder(orderManagementId,request.getEstimatedCookTime());

        return ApiResponse.success(SUCCESS, null);
    }

    @PatchMapping("/{orderManagementId}/reject")
    public ApiResponse<Void> rejectOrder( @PathVariable UUID orderManagementId,@RequestBody @Valid OrderRejectRequest request) {

        orderManagementService.rejectOrder(orderManagementId,request.getRejectReasonCodeId(),request.getMemo());

        return ApiResponse.success(SUCCESS, null);
    }

    @PatchMapping("/{orderManagementId}/status")
    public ApiResponse<Void> updateStatus(@PathVariable UUID orderManagementId,@RequestBody @Valid OrderStatusUpdateRequest request) {

        orderManagementService.updateStatus(orderManagementId,request.getStatus());

        return ApiResponse.success(SUCCESS,null);
    }
}
