package com.sparta.server.threeserving.order.controller;

import com.sparta.server.threeserving.auth.UserDetailsImpl;
import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.order.dto.request.OrderCreateRequestDto;
import com.sparta.server.threeserving.order.dto.response.OrderCreateResponseDto;
import com.sparta.server.threeserving.order.dto.response.OrderDetailResponseDto;
import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order.service.OrderSearchCondition;
import com.sparta.server.threeserving.order.service.OrderService;
import com.sparta.server.threeserving.user.entity.User;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/")
    public ApiResponse<OrderCreateResponseDto> createOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody OrderCreateRequestDto orderCreateRequestDto
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
        if(userRoleEnum != UserRoleEnum.CUSTOMER && userRoleEnum != UserRoleEnum.MANAGER && userRoleEnum != UserRoleEnum.MASTER){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        return orderService.getOrderDetail(userId, orderId);
    }

    @GetMapping("/")
    public ApiResponse<OrderDetailResponseDto> getOrderList(
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
                user, new OrderSearchCondition(storeId, userId, orderStatusEnum, size, page - 1, sortBy, isAsc));
    }

    private Long requireCartAccessibleUserId(UserDetailsImpl userDetails) {
        if(userDetails == null){
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return userDetails.getUser().getId();
    }
}
