package com.sparta.server.threeserving.order.controller;

import com.sparta.server.threeserving.auth.UserDetailsImpl;
import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.order.dto.request.CartAddItemRequestDto;
import com.sparta.server.threeserving.order.dto.request.CartCreateRequestDto;
import com.sparta.server.threeserving.order.dto.request.CartUpdateItemAmountRequestDto;
import com.sparta.server.threeserving.order.dto.request.CheckoutRequestDto;
import com.sparta.server.threeserving.order.dto.response.*;
import com.sparta.server.threeserving.order.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/carts")
public class CartController {
    private final CartService cartService;

    @PostMapping("")
    public ApiResponse<CartResponseDto> createOrFindCart(
            @RequestBody @Valid CartCreateRequestDto cartCreateRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
            ){
        Long userId = requireCartAccessibleUserId(userDetails);
        return cartService.createOrFindCart(userId, cartCreateRequestDto.storeId());
    }

    @GetMapping("")
    public ApiResponse<List<CartListResponseDto>> getCartList(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        Long userId = requireCartAccessibleUserId(userDetails);
        return cartService.getCartList(userId);
    }

    @GetMapping("/{cartId}")
    public ApiResponse<CartDetailResponseDto> getCartDetail(
            @PathVariable UUID cartId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        Long userId = requireCartAccessibleUserId(userDetails);
        return cartService.getCartDetail(userId, cartId);
    }


    @PostMapping("/{cartId}/items")
    public ApiResponse<CartAddItemResponseDto> addMenuToCart(
            @PathVariable UUID cartId,
            @RequestBody @Valid CartAddItemRequestDto cartAddItemRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
            ){
        Long userId = requireCartAccessibleUserId(userDetails);
        return cartService.addMenuToCart(userId, cartId, cartAddItemRequestDto);
    }


    @PatchMapping("/{cartId}/items/{cartItemId}")
    public ApiResponse<CartUpdateItemAmountResponseDto> updateCartItemAmount(
            @PathVariable UUID cartId,
            @PathVariable UUID cartItemId,
            @RequestBody @Valid CartUpdateItemAmountRequestDto cartUpdateItemAmountRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
            ) {
        Long userId = requireCartAccessibleUserId(userDetails);
        return cartService.updateCartItemAmount(userId, cartId, cartItemId, cartUpdateItemAmountRequestDto);
    }

    @DeleteMapping("/{cartId}/items/{cartItemId}")
    public ApiResponse<Void> deleteCartItem(
            @PathVariable UUID cartId,
            @PathVariable UUID cartItemId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = requireCartAccessibleUserId(userDetails);
        return cartService.deleteCartItem(userId, cartId, cartItemId);
    }

    @PostMapping("/{cartId}/checkout")
    public ApiResponse<CheckoutResponseDto> checkout(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID cartId,
            @RequestBody @Valid CheckoutRequestDto requestDto
            ){
        Long userId = requireCartAccessibleUserId(userDetails);
        return cartService.checkout(userId, cartId, requestDto);
    }

    private Long requireCartAccessibleUserId(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return userDetails.getUser().getId();
    }
}
