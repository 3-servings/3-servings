package com.sparta.server.threeserving.order.controller;

import com.sparta.server.threeserving.auth.UserDetailsImpl;
import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.order.dto.OrderCancelResponseDto;
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
    // TODO: Security Config으로 user 접근제한 바꾸기

    @PostMapping("")
    public ApiResponse<OrderCreateResponseDto> createOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid OrderCreateRequestDto orderCreateRequestDto
    ){
        UserRoleEnum userRoleEnum = userDetails.getUser().getRole();
        if(userRoleEnum != UserRoleEnum.MANAGER && userRoleEnum != UserRoleEnum.MASTER){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        return orderService.createOrder(orderCreateRequestDto);
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponseDto> getOrderDetail(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID orderId
    ) {
        UserRoleEnum userRoleEnum = userDetails.getUser().getRole();
        Long userId = requireCartAccessibleUserId(userDetails);
        return orderService.getOrderDetail(userId, userRoleEnum, orderId);
    }

    @GetMapping("")
    public ApiResponse<Page<OrderListResponseDto>> getOrderList(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(value = "storeId", required = false) UUID storeId,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "status") OrderStatusEnum orderStatusEnum,
            @RequestParam("size") int size,
            @RequestParam("page") int page,
            @RequestParam("sort") String sortBy,
            @RequestParam("isAsc") boolean isAsc
    ){
        requireCartAccessibleUserId(userDetails);
        User user = userDetails.getUser();
        return orderService.getOrderList(
                user, storeId, userId, orderStatusEnum, size, page - 1, sortBy, isAsc);
    }

    @PatchMapping("/{orderId}")
    public ApiResponse<OrderModifyResponseDto> modifyOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID orderId,
            @RequestBody OrderModifyRequestDto orderModifyRequestDto
    ){
        Long userId = requireCartAccessibleUserId(userDetails);
        return orderService.modifyOrderInfo(userId, orderId, orderModifyRequestDto);
    }

    @PatchMapping("/{orderId}/cancel")
    public ApiResponse<OrderCancelResponseDto> CancelOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID orderId
    ){
        Long userId = requireCartAccessibleUserId(userDetails);
        return orderService.cancelOrder(userId, orderId);
    }

    @DeleteMapping("/{orderId}")
    public ApiResponse<Void> DeleteOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID orderId
    ){
        UserRoleEnum userRoleEnum = userDetails.getUser().getRole();
        if(userRoleEnum != UserRoleEnum.MANAGER && userRoleEnum != UserRoleEnum.MASTER){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        Long userId = userDetails.getUser().getId();
        return orderService.deleteOrder(userId, orderId);
    }


    private Long requireCartAccessibleUserId(UserDetailsImpl userDetails) {
        if(userDetails == null){
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return userDetails.getUser().getId();
    }
}
