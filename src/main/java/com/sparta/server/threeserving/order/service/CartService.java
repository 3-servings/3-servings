package com.sparta.server.threeserving.order.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.menu.entity.Menu;
import com.sparta.server.threeserving.menu.entity.OptionItem;
import com.sparta.server.threeserving.menu.repository.MenuRepository;
import com.sparta.server.threeserving.menu.repository.OptionItemRepository;
import com.sparta.server.threeserving.order.dto.response.*;
import com.sparta.server.threeserving.order.dto.request.CartUpdateItemAmountRequestDto;
import com.sparta.server.threeserving.order.dto.request.CartAddItemRequestDto;
import com.sparta.server.threeserving.order.dto.request.CartItemOptionRequestDto;
import com.sparta.server.threeserving.order.entity.Cart;
import com.sparta.server.threeserving.order.entity.CartItem;
import com.sparta.server.threeserving.order.entity.CartItemOption;
import com.sparta.server.threeserving.order.repository.CartItemOptionRepository;
import com.sparta.server.threeserving.order.repository.CartItemRepository;
import com.sparta.server.threeserving.order.repository.CartRepository;
import com.sparta.server.threeserving.store.entity.Store;
import com.sparta.server.threeserving.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final StoreRepository storeRepository;
    private final CartItemOptionRepository cartItemOptionRepository;
    private final MenuRepository menuRepository;
    private final OptionItemRepository optionItemRepository;

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

        // 카트에 담긴 항목(메뉴) 전체
        List<CartItem> cartItems = cartItemRepository.findAllByCart_IdAndDeletedAtIsNull(cartId);
        if (cartItems.isEmpty()) {
            return ApiResponse.success(SuccessCode.SUCCESS, new CartDetailResponseDto(cart, 0, List.of()));
        }

        // 1. 카트Item에 따라서 메뉴명 조회 -> Map<menuId, Menu> 필요
        // 2. 카트Item의 OptionList의 price*quantity 합산 -> Map<cartItem, List<OptionItem>>, Map<optionId, OptionItem>> 필요
        // 3. total_price계산 / 각 카트별 메뉴 수, 메뉴별 option 수가 100개 미만이라고 치면 합리적인 latency 나옴
        // 더 최적화 할 방안 찾기

        // 메뉴 배치 조회 후 id로 매핑
        List<UUID> menuIds = cartItems.stream().map(CartItem::getMenuId).distinct().toList();
        Map<UUID, Menu> menuById = menuRepository.findAllById(menuIds).stream()
                .collect(Collectors.toMap(Menu::getId, Function.identity()));

        // 항목별 옵션을 한 번에 배치 조회 후 cartItemId 기준으로 묶기
        List<CartItemOption> cartItemOptions = cartItemOptionRepository.findAllByCartItemIn(cartItems);
        Map<UUID, List<CartItemOption>> optionsByCartItemId = cartItemOptions.stream()
                .collect(Collectors.groupingBy(option -> option.getCartItem().getId()));

        // 옵션 아이템 배치 조회 후 id로 매핑
        List<UUID> optionItemIds = cartItemOptions.stream().map(CartItemOption::getOptionItemId).distinct().toList();
        Map<UUID, OptionItem> optionItemById = optionItemRepository.findAllById(optionItemIds).stream()
                .collect(Collectors.toMap(OptionItem::getId, Function.identity()));

        List<CartItemDetailResponseDto> totalItemList = new ArrayList<>();
        int estimatedTotalPrice = 0;

        for (CartItem cartItem : cartItems) {
            Menu menu = menuById.get(cartItem.getMenuId());
            if (menu == null) {
                // 카트의 메뉴Id가 지워졌거나 잘못된 id저장.
                throw new CustomException(ErrorCode.MENU_NOT_FOUND);
            }

            List<CartItemOptionRequestDto> optionDtos = new ArrayList<>();
            int optionUnitPrice = 0;
            for (CartItemOption option : optionsByCartItemId.getOrDefault(cartItem.getId(), List.of())) {
                OptionItem optionItem = optionItemById.get(option.getOptionItemId());
                if (optionItem == null) {
                    // 담긴 뒤 삭제된 옵션 - 조회 화면에서는 조용히 제외
                    continue;
                }
                optionDtos.add(new CartItemOptionRequestDto(
                        optionItem.getId(), optionItem.getName(), option.getQuantity()));
                optionUnitPrice += optionItem.getPrice() * option.getQuantity();
            }

            totalItemList.add(new CartItemDetailResponseDto(
                    cartItem.getId(), cartItem.getMenuId(), menu.getName(), cartItem.getQuantity(), optionDtos));

            estimatedTotalPrice += (menu.getPrice() + optionUnitPrice) * cartItem.getQuantity();
        }

        CartDetailResponseDto responseDto = new CartDetailResponseDto(cart, estimatedTotalPrice, totalItemList);
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
                .map(optionItemId -> new CartItemOption(cartItem, optionItemId, 1))
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
