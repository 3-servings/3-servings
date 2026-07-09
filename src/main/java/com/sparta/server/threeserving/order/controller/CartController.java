package com.sparta.server.threeserving.order.controller;

import com.sparta.server.threeserving.auth.UserDetailsImpl;
import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.order.dto.response.*;
import com.sparta.server.threeserving.order.dto.request.CartUpdateItemAmountRequestDto;
import com.sparta.server.threeserving.order.dto.request.CartAddItemRequestDto;
import com.sparta.server.threeserving.order.dto.request.CartCreateRequestDto;
import com.sparta.server.threeserving.order.service.CartService;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class CartController {
    private final CartService cartService;

    @PostMapping("/carts")
    public ApiResponse<CartResponseDto> createOrFindCart(
            @RequestBody @Valid CartCreateRequestDto cartCreateRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
            ){
        UserRoleEnum userRoleEnum = userDetails.getUser().getRole();
        Long userId = requireCartAccessibleUserId(userDetails);
        if(userRoleEnum != UserRoleEnum.CUSTOMER && userRoleEnum != UserRoleEnum.MASTER && userRoleEnum != UserRoleEnum.MANAGER
        ){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        return cartService.createOrFindCart(userId, cartCreateRequestDto.storeId());
    }

    @GetMapping("/carts")
    public ApiResponse<List<CartListResponseDto>> getCartList(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        UserRoleEnum userRoleEnum = userDetails.getUser().getRole();
        Long userId = requireCartAccessibleUserId(userDetails);
        if(userRoleEnum != UserRoleEnum.CUSTOMER && userRoleEnum != UserRoleEnum.MASTER && userRoleEnum != UserRoleEnum.MANAGER
        ){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        return cartService.getCartList(userId);
    }

    @GetMapping("/carts/{cartId}")
    public ApiResponse<CartDetailResponseDto> getCartDetail(
            @PathVariable UUID cartId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        UserRoleEnum userRoleEnum = userDetails.getUser().getRole();
        Long userId = requireCartAccessibleUserId(userDetails);
        if(userRoleEnum != UserRoleEnum.CUSTOMER && userRoleEnum != UserRoleEnum.MASTER && userRoleEnum != UserRoleEnum.MANAGER
        ){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        return cartService.getCartDetail(userId, cartId);
    }


    @PostMapping("/carts/{cartId}/items")
    public ApiResponse<CartAddItemResponseDto> addMenuToCart(
            @PathVariable UUID cartId,
            @RequestBody @Valid CartAddItemRequestDto cartAddItemRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
            ){
        UserRoleEnum userRoleEnum = userDetails.getUser().getRole();
        Long userId = requireCartAccessibleUserId(userDetails);
        if(userRoleEnum != UserRoleEnum.CUSTOMER && userRoleEnum != UserRoleEnum.MASTER && userRoleEnum != UserRoleEnum.MANAGER
        ){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        return cartService.addMenuToCart(userId, cartId, cartAddItemRequestDto);
    }


    @PatchMapping("/carts/{cartId}/items/{cartItemId}")
    public ApiResponse<CartUpdateItemAmountResponseDto> updateCartItemAmount(
            @PathVariable UUID cartId,
            @PathVariable UUID cartItemId,
            @RequestBody @Valid CartUpdateItemAmountRequestDto cartUpdateItemAmountRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
            ) {
        UserRoleEnum userRoleEnum = userDetails.getUser().getRole();
        Long userId = requireCartAccessibleUserId(userDetails);
        if(userRoleEnum != UserRoleEnum.CUSTOMER && userRoleEnum != UserRoleEnum.MASTER && userRoleEnum != UserRoleEnum.MANAGER
        ){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        return cartService.updateCartItemAmount(userId, cartId, cartItemId, cartUpdateItemAmountRequestDto);
    }

    @DeleteMapping("/carts/{cartId}/items/{cartItemId}")
    public ApiResponse<Void> deleteCartItem(
            @PathVariable UUID cartId,
            @PathVariable UUID cartItemId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        UserRoleEnum userRoleEnum = userDetails.getUser().getRole();
        Long userId = requireCartAccessibleUserId(userDetails);
        if(userRoleEnum != UserRoleEnum.CUSTOMER && userRoleEnum != UserRoleEnum.MASTER && userRoleEnum != UserRoleEnum.MANAGER
        ){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        return cartService.deleteCartItem(userId, cartId, cartItemId);
    }

    private Long requireCartAccessibleUserId(UserDetailsImpl userDetails) {
        if(userDetails == null){
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return userDetails.getUser().getId();
    }
}
