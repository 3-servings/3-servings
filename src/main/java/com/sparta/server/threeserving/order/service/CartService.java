package com.sparta.server.threeserving.order.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.order.dto.response.*;
import com.sparta.server.threeserving.order.dto.request.CartUpdateItemAmountRequestDto;
import com.sparta.server.threeserving.order.dto.request.CartAddItemRequestDto;
import com.sparta.server.threeserving.order.dto.request.CartItemOptionDto;
import com.sparta.server.threeserving.order.entity.Cart;
import com.sparta.server.threeserving.order.entity.CartItem;
import com.sparta.server.threeserving.order.entity.CartItemOption;
import com.sparta.server.threeserving.order.repository.CartItemOptionRepository;
import com.sparta.server.threeserving.order.repository.CartItemRepository;
import com.sparta.server.threeserving.order.repository.CartRepository;
import com.sparta.server.threeserving.order.repository.StoreRepository;
import com.sparta.server.threeserving.store.Store;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final StoreRepository storeRepository;
    private final CartItemOptionRepository cartItemOptionRepository;

    @Transactional
    public ApiResponse<CartResponseDto> createOrFindCart(Long userId, UUID storeId) {
        storeRepository.findById(storeId).orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 해당 가게 활성 카드 확인
        Optional<Cart> existingCart = cartRepository.findByUserIdAndStoreIdAndDeletedAtIsNull(userId, storeId);
        boolean isExist = existingCart.isPresent();
        Cart cart = existingCart.orElseGet(() -> new Cart(userId, storeId));

        cartRepository.save(cart);
        CartResponseDto responseDto = new CartResponseDto(cart);
        SuccessCode successCode = isExist ? SuccessCode.SUCCESS : SuccessCode.CREATED;
        return ApiResponse.success(successCode, responseDto);
    }

    public ApiResponse<List<CartListResponseDto>> getCartList(Long userId) {
        // N+1 문제 발생 및 해결
        List<Cart> cartList = cartRepository.findAllByUserIdAndDeletedAtIsNull(userId);
        if (cartList.isEmpty()) {
            return ApiResponse.success(SuccessCode.SUCCESS, List.of());
        }

        List<UUID> storeIds = cartList.stream().map(Cart::getStoreId).distinct().toList();
        Map<UUID, Store> storeById = storeRepository.findAllById(storeIds).stream()
                .collect(Collectors.toMap(Store::getId, Function.identity()));

        List<UUID> cartIds = cartList.stream().map(Cart::getId).toList();
        Map<UUID, Long> itemCountByCartId = cartItemRepository.countByCartIdIn(cartIds)
                .stream().collect(
                        Collectors.toMap(CartItemRepository.CartItemCountProjection::getCartId,
                                CartItemRepository.CartItemCountProjection::getCount));
        List<CartListResponseDto> result = cartList.stream()
                .map(cart -> new CartListResponseDto(
                        cart,
                        storeById.get(cart.getStoreId()).getName(),
                        itemCountByCartId.getOrDefault(cart.getId(), 0L)
                ))
                .toList();

        return ApiResponse.success(SuccessCode.SUCCESS, result);
    }

    public ApiResponse<CartDetailResponseDto> getCartDetail(Long userId, UUID cartId) {
        Cart cart = validateCartOwner(userId, cartId);

        // 카트에 담긴 항목 전체 (1쿼리)
        List<CartItem> cartItems = cartItemRepository.findAllByCart_IdAndDeletedAtIsNull(cartId);

        // 항목별 옵션을 한 번에 배치 조회 후 cartItemId 기준으로 묶기 (1쿼리, N+1 방지)
        Map<UUID, List<CartItemOption>> optionsByCartItemId = cartItemOptionRepository
                .findAllByCartItemIn(cartItems).stream()
                .collect(Collectors.groupingBy(option -> option.getCartItem().getId()));

        // TODO: Menu 도메인 완성되면 menuRepository/optionItemRepository로 menu_name, option_name, 실제 가격 조회
        List<CartItemDetailDto> items = cartItems.stream()
                .map(cartItem -> new CartItemDetailDto(
                        cartItem.getId(),
                        cartItem.getMenuId(),
                        null,
                        cartItem.getQuantity(),
                        optionsByCartItemId.getOrDefault(cartItem.getId(), List.of()).stream()
                                .map(option -> new CartItemOptionDto(option.getOptionItemId(), null))
                                .toList()
                ))
                .toList();

        // TODO: Menu/OptionItem 가격 연동되면 실제 예상 총액으로 교체
        Integer estimatedTotalPrice = 0;

        CartDetailResponseDto responseDto = new CartDetailResponseDto(cart, estimatedTotalPrice, items);
        return ApiResponse.success(SuccessCode.SUCCESS, responseDto);
    }

    @Transactional
    public ApiResponse<CartAddItemResponseDto> addMenuToCart(Long userId, UUID cartId, CartAddItemRequestDto cartAddItemRequestDto) {
        Cart cart = validateCartOwner(userId, cartId);

        // TODO: Menu id로 Menu 존재/soldout/store 일치 확인
        // Menu menu = menuRepository.findById(menuId).orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));
        UUID menuId = cartAddItemRequestDto.menuId();

        // 옵션 조합이 다를 수 있으므로 기존 항목과 병합하지 않고 매번 새 cart_item으로 생성
        CartItem cartItem = new CartItem(cart, menuId, cartAddItemRequestDto.quantity());
        cartItemRepository.save(cartItem);

        // TODO: 각 optionItemIds가 실제로 해당 메뉴의 옵션 그룹에 속하는지, 단일선택 그룹 규칙 위반 여부 확인
        List<CartItemOption> cartItemOptions = cartAddItemRequestDto.optionItemIds().stream()
                .map(optionItemId -> new CartItemOption(cartItem, optionItemId))
                .toList();
        cartItemOptionRepository.saveAll(cartItemOptions);

        CartAddItemResponseDto responseDto = new CartAddItemResponseDto(
                cartItem.getId(), cart.getId(), menuId, cartItem.getQuantity());
        return ApiResponse.success(SuccessCode.CREATED, responseDto);
    }

    @Transactional
    public ApiResponse<CartUpdateItemAmountResponseDto> updateCartItemAmount(Long userId, UUID cartId, UUID cartItemId, CartUpdateItemAmountRequestDto cartUpdateItemAmountRequestDto) {
        validateCartOwner(userId, cartId);
        CartItem cartItem = cartItemRepository.findByIdAndCart_IdAndDeletedAtIsNull(cartItemId, cartId)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_ITEM_NOT_FOUND));
        cartItem.setQuantity(cartUpdateItemAmountRequestDto.quantity());
        CartUpdateItemAmountResponseDto responseDto = new CartUpdateItemAmountResponseDto(cartItem.getId(), cartItem.getQuantity());
        return ApiResponse.success(SuccessCode.SUCCESS, responseDto);
    }

    @Transactional
    public ApiResponse<Void> deleteCartItem(Long userId, UUID cartId, UUID cartItemId) {
        validateCartOwner(userId, cartId);
        CartItem cartItem = cartItemRepository.findByIdAndCart_IdAndDeletedAtIsNull(cartItemId, cartId)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_ITEM_NOT_FOUND));

        // p_cart_item_option 테이블엔 deleted_at 컬럼이 없어 soft delete 불가 -> 딸린 옵션은 하드 삭제
        List<CartItemOption> options = cartItemOptionRepository.findAllByCartItemIn(List.of(cartItem));
        cartItemOptionRepository.deleteAll(options);

        cartItem.softDelete(userId);
        return ApiResponse.success(SuccessCode.DELETED);
    }

    private Cart validateCartOwner(Long userId, UUID cartId) {
        Cart cart = cartRepository.findByIdAndDeletedAtIsNull(cartId)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));

        if (!cart.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_CART_OWNER);
        }
        return cart;
    }
}
