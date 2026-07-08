package com.sparta.server.threeserving.order.controller;

import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.order.dto.response.*;
import com.sparta.server.threeserving.order.dto.request.CartUpdateItemAmountRequestDto;
import com.sparta.server.threeserving.order.dto.request.CartAddItemRequestDto;
import com.sparta.server.threeserving.order.dto.request.CartCreateRequestDto;
import com.sparta.server.threeserving.order.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
            @RequestBody @Valid CartCreateRequestDto cartCreateRequestDto
            ){
        // TODO: User - Customer인지 로그인 확인 로직
        Long userId = 0L;
        return cartService.createOrFindCart(userId, cartCreateRequestDto.storeId());
    }

    @GetMapping("/carts")
    public ApiResponse<List<CartListResponseDto>> getCartList(){
        // TODO: User - Customer인지 로그인 확인 로직
        Long userId = 0L;
        return cartService.getCartList(userId);
    }

    @GetMapping("/carts/{cartId}")
    public ApiResponse<CartDetailResponseDto> getCartDetail(
            @PathVariable UUID cartId
    ){
        // TODO: User - Customer인지 로그인 확인 로직
        Long userId = 0L;
        return cartService.getCartDetail(userId, cartId);
    }


    @PostMapping("/carts/{cartId}/items")
    public ApiResponse<CartAddItemResponseDto> addMenuToCart(
            @PathVariable UUID cartId,
            @RequestBody @Valid CartAddItemRequestDto cartAddItemRequestDto
            ){
        // TODO: User - Customer인지 로그인 확인 로직
        Long userId = 0L;
        return cartService.addMenuToCart(userId, cartId, cartAddItemRequestDto);
    }


    @PatchMapping("/carts/{cartId}/items/{cartItemId}")
    public ApiResponse<CartUpdateItemAmountResponseDto> updateCartItemAmount(
            @PathVariable UUID cartId,
            @PathVariable UUID cartItemId,
            @RequestBody @Valid CartUpdateItemAmountRequestDto cartUpdateItemAmountRequestDto
            ) {
        // TODO: User - Customer인지 로그인 확인 로직
        Long userId = 0L;
        return cartService.updateCartItemAmount(userId, cartId, cartItemId, cartUpdateItemAmountRequestDto);
    }

    @DeleteMapping("/carts/{cartId}/items/{cartItemId}")
    public ApiResponse<Void> deleteCartItem(
            @PathVariable UUID cartId,
            @PathVariable UUID cartItemId
    ) {
        // TODO: User - Customer인지 로그인 확인 로직
        Long userId = 0L;
        return cartService.deleteCartItem(userId, cartId, cartItemId);
    }
}
