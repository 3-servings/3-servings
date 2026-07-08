package com.sparta.server.threeserving.order.controller;

import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.order.dto.CartAddItemResponseDto;
import com.sparta.server.threeserving.order.dto.request.CartAddItemRequestDto;
import com.sparta.server.threeserving.order.dto.request.CartCreateRequestDto;
import com.sparta.server.threeserving.order.dto.response.CartDetailResponseDto;
import com.sparta.server.threeserving.order.dto.response.CartListResponseDto;
import com.sparta.server.threeserving.order.dto.response.CartResponseDto;
import com.sparta.server.threeserving.order.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
        // User - Customer인지 로그인 확인
        Long userId = 0L;
        return cartService.getCartList(userId);
    }

    @GetMapping("/carts/{cartId}")
    public ApiResponse<CartDetailResponseDto> getCartDetail(
            @PathVariable UUID cartId
    ){
        // User - Customer인지 로그인 확인
        Long userId = 0L;
        return cartService.getCartDetail(userId, cartId);
    }


    @PostMapping("/carts/{cartId}/items")
    public ApiResponse<CartAddItemResponseDto> addMenuToCart(
            @PathVariable UUID cartId,
            @RequestBody CartAddItemRequestDto cartAddItemRequestDto
            ){
        // User - Customer인지 로그인 확인
        Long userId = 0L;
        return cartService.addMenuToCart(userId, cartId, cartAddItemRequestDto);
    }
}
