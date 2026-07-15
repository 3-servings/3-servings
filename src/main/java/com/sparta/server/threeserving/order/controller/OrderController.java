package com.sparta.server.threeserving.order.controller;

import com.sparta.server.threeserving.auth.UserDetailsImpl;
import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.order.dto.response.OrderCancelResponseDto;
import com.sparta.server.threeserving.order.dto.request.OrderCreateRequestDto;
import com.sparta.server.threeserving.order.dto.request.OrderModifyRequestDto;
import com.sparta.server.threeserving.order.dto.response.OrderCreateResponseDto;
import com.sparta.server.threeserving.order.dto.response.OrderDetailResponseDto;
import com.sparta.server.threeserving.order.dto.response.OrderListResponseDto;
import com.sparta.server.threeserving.order.dto.response.OrderModifyResponseDto;
import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order.service.OrderService;
import com.sparta.server.threeserving.user.entity.User;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("")
    public ApiResponse<OrderCreateResponseDto> createOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid OrderCreateRequestDto orderCreateRequestDto
    ){
        if(userDetails == null)
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        return ApiResponse.success(SuccessCode.CREATED, orderService.createOrder(orderCreateRequestDto));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponseDto> getOrderDetail(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID orderId
    ) {
        Long userId = requireCartAccessibleUserId(userDetails);
        UserRoleEnum userRoleEnum = userDetails.getUser().getRole();
        return ApiResponse.success(SuccessCode.SUCCESS, orderService.getOrderDetail(userId, userRoleEnum, orderId));
    }

    @GetMapping("")
    public ApiResponse<Page<OrderListResponseDto>> getOrderList(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(value = "storeId", required = false) UUID storeId,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "status", required = false) OrderStatusEnum orderStatusEnum,
            @RequestParam(value = "size", required = false) int size,
            @RequestParam(value = "page", required = false) int page,
            @RequestParam(value = "sort", required = false) String sortBy,
            @RequestParam(value = "isAsc", required = false) boolean isAsc
    ){
        requireCartAccessibleUserId(userDetails);
        User user = userDetails.getUser();
        return ApiResponse.success(SuccessCode.SUCCESS, orderService.getOrderList(
                user, storeId, userId, orderStatusEnum, size, page , sortBy, isAsc));
    }

    @PatchMapping("/{orderId}")
    public ApiResponse<OrderModifyResponseDto> modifyOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID orderId,
            @RequestBody @Valid OrderModifyRequestDto orderModifyRequestDto
    ){
        Long userId = requireCartAccessibleUserId(userDetails);
        return ApiResponse.success(SuccessCode.SUCCESS,
                orderService.modifyOrderInfo(userId, orderId, orderModifyRequestDto));
    }

    @PatchMapping("/{orderId}/cancel")
    public ApiResponse<OrderCancelResponseDto> cancelOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID orderId
    ){
        Long userId = requireCartAccessibleUserId(userDetails);
        return ApiResponse.success(SuccessCode.SUCCESS, orderService.cancelOrder(userId, orderId));
    }

    @DeleteMapping("/{orderId}")
    public ApiResponse<Void> deleteOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID orderId
    ){
        Long userId = requireCartAccessibleUserId(userDetails);
        return orderService.deleteOrder(userId, orderId);
    }


    private Long requireCartAccessibleUserId(UserDetailsImpl userDetails) {
        if(userDetails == null){
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return userDetails.getUser().getId();
    }
}
