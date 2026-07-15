package com.sparta.server.threeserving.order.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.menu.entity.*;
import com.sparta.server.threeserving.menu.enums.MenuStatus;
import com.sparta.server.threeserving.menu.repository.MenuOptionGroupRepository;
import com.sparta.server.threeserving.menu.repository.MenuRepository;
import com.sparta.server.threeserving.menu.repository.OptionItemRepository;
import com.sparta.server.threeserving.order.dto.request.CartAddItemRequestDto;
import com.sparta.server.threeserving.order.dto.request.CartItemOptionRequestDto;
import com.sparta.server.threeserving.order.dto.request.CartUpdateItemAmountRequestDto;
import com.sparta.server.threeserving.order.dto.request.CheckoutRequestDto;
import com.sparta.server.threeserving.order.dto.response.*;
import com.sparta.server.threeserving.order.entity.*;
import com.sparta.server.threeserving.order.repository.*;
import com.sparta.server.threeserving.order_management.service.OrderManagementService;
import com.sparta.server.threeserving.store.entity.Store;
import com.sparta.server.threeserving.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemOptionRepository cartItemOptionRepository;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemOptionRepository orderItemOptionRepository;

    private final OrderManagementService orderManagementService;

    private final StoreRepository storeRepository;

    private final MenuRepository menuRepository;
    private final OptionItemRepository optionItemRepository;
    private final MenuOptionGroupRepository menuOptionGroupRepository;

    private record CartLineItem(CartItem cartItem, Menu menu, List<OptionItem> options) {}

    @Transactional
    public ApiResponse<CartResponseDto> createOrFindCart(Long userId, UUID storeId) {
        storeRepository.findById(storeId).orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 해당 가게 활성 카트 확인
        Optional<Cart> existingCart = cartRepository.findByUserIdAndStoreIdAndDeletedAtIsNull(userId, storeId);
        boolean isExist = existingCart.isPresent();
        Cart cart = existingCart.orElseGet(() -> new Cart(userId, storeId));

        cartRepository.save(cart);
        CartResponseDto responseDto = new CartResponseDto(cart);
        SuccessCode successCode = isExist ? SuccessCode.SUCCESS : SuccessCode.CREATED;
        return ApiResponse.success(successCode, responseDto);
    }

    public List<CartListResponseDto> getCartList(Long userId) {
        List<Cart> cartList = cartRepository.findAllByUserIdAndDeletedAtIsNull(userId);
        if (cartList.isEmpty()) {
            return List.of();
        }

        List<UUID> storeIds = cartList.stream().map(Cart::getStoreId).distinct().toList();
        Map<UUID, Store> storeById = storeRepository.findAllById(storeIds).stream()
                .collect(Collectors.toMap(Store::getId, Function.identity()));

        List<UUID> cartIds = cartList.stream().map(Cart::getId).toList();
        Map<UUID, Long> itemCountByCartId = cartItemRepository.countByCartIdIn(cartIds)
                .stream().collect(
                        Collectors.toMap(CartItemRepository.CartItemCountProjection::getCartId,
                                CartItemRepository.CartItemCountProjection::getCount));

        return cartList.stream()
                .map(cart -> new CartListResponseDto(
                        cart,
                        storeById.get(cart.getStoreId()).getName(),
                        itemCountByCartId.getOrDefault(cart.getId(), 0L)
                ))
                .toList();
    }

    public CartDetailResponseDto getCartDetail(Long userId, UUID cartId) {
        Cart cart = validateCartOwner(userId, cartId);

        // 카트에 담긴 항목(메뉴) 전체
        List<CartItem> cartItems = cartItemRepository.findAllByCart_IdAndDeletedAtIsNull(cartId);
        if (cartItems.isEmpty()) {
            return new CartDetailResponseDto(cart, 0, List.of());
        }

        List<CartLineItem> cartLineItem = getCartLineItems(cartItems);

        List<CartItemDetailResponseDto> totalItemList = new ArrayList<>();
        int estimatedTotalPrice = 0;

        for (CartLineItem lineItem : cartLineItem) {
            totalItemList.add(
                    new CartItemDetailResponseDto(
                            lineItem.cartItem().getId(),
                            lineItem.menu().getId(),
                            lineItem.menu().getName(),
                            lineItem.cartItem().getQuantity(),
                            lineItem.options().stream().map(
                                    optionItem ->
                                            new CartItemOptionRequestDto(optionItem.getId(), optionItem.getName())
                            ).toList()
                    )
            );

            int optionPriceSum = lineItem.options().stream().mapToInt(OptionItem::getPrice).sum();
            estimatedTotalPrice += (lineItem.menu().getPrice() + optionPriceSum) * lineItem.cartItem().getQuantity();
        }

        return new CartDetailResponseDto(cart, estimatedTotalPrice, totalItemList);
    }

    @NonNull
    private List<CartLineItem> getCartLineItems(List<CartItem> cartItemList) {
        // 메뉴 배치 조회 후 id로 매핑
        List<UUID> menuIds = cartItemList.stream().map(CartItem::getMenuId).distinct().toList();
        Map<UUID, Menu> menuById = menuRepository.findAllById(menuIds).stream()
                .collect(Collectors.toMap(Menu::getId, Function.identity()));

        // 항목별 옵션을 한 번에 배치 조회 후 cartItemId 기준으로 묶기 (soft delete된 옵션은 제외)
        List<CartItemOption> cartItemOptions = cartItemOptionRepository.findAllByCartItemInAndDeletedAtIsNull(cartItemList);
        Map<UUID, List<CartItemOption>> optionsByCartItemId = cartItemOptions.stream()
                .collect(Collectors.groupingBy(option -> option.getCartItem().getId()));

        // 옵션 아이템 배치 조회 후 id로 매핑
        // 옵션은 p_cart_item_option에 quantity 컬럼이 없음 -> 수량이 아니라 "선택 여부"만 존재
        List<UUID> optionItemIds = cartItemOptions.stream().map(CartItemOption::getOptionItemId).distinct().toList();
        Map<UUID, OptionItem> optionItemById = optionItemRepository.findAllById(optionItemIds).stream()
                .collect(Collectors.toMap(OptionItem::getId, Function.identity()));

        return cartItemList.stream()
                .map(cartItem -> {
                    Menu menu = menuById.get(cartItem.getMenuId());
                    if (menu == null) {
                        throw new CustomException(ErrorCode.MENU_NOT_FOUND);
                    }
                    List<OptionItem> options = optionsByCartItemId.
                            getOrDefault(cartItem.getId(), List.of()).stream()
                            .map(option -> optionItemById.get(option.getOptionItemId()))
                            .filter(Objects::nonNull)
                            .toList();
                    return new CartLineItem(cartItem, menu, options);
                }).toList();
    }

    @Transactional
    public CartAddItemResponseDto addMenuToCart(Long userId, UUID cartId, CartAddItemRequestDto cartAddItemRequestDto) {
        Cart cart = validateCartOwner(userId, cartId);

        // Menu id로 Menu 존재/soldout/store 일치 확인
        UUID menuId = cartAddItemRequestDto.menuId();
        Menu menu = menuRepository.findById(menuId).orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));
        if(menu.getStatus() != MenuStatus.AVAILABLE) {
            throw new CustomException(ErrorCode.ADD_UNAVAILABLE_MENU_TO_CART);
        }
        if(!menu.getStore().getId().equals(cart.getStoreId())){
            throw new CustomException(ErrorCode.ADD_MENU_OF_OTHER_STORE_TO_CART);
        }

        // 옵션은 수량 개념이 없는 "선택" 단위라 같은 옵션을 두 번 담는 요청은 의미가 없음 -> 중복 제거
        List<UUID> optionItemIds = cartAddItemRequestDto.optionItemIds().stream().distinct().toList();
        validateOptionSelection(menuId, optionItemIds);

        // 옵션 조합이 다를 수 있으므로 기존 항목과 병합하지 않고 매번 새 cart_item으로 생성
        CartItem cartItem = new CartItem(cart, menuId, cartAddItemRequestDto.quantity());
        cartItemRepository.save(cartItem);

        List<CartItemOption> cartItemOptions = optionItemIds.stream()
                .map(optionItemId -> new CartItemOption(cartItem, optionItemId))
                .toList();
        cartItemOptionRepository.saveAll(cartItemOptions);

        return new CartAddItemResponseDto(
                cartItem.getId(), cart.getId(), menuId, cartItem.getQuantity());
    }

    // 메뉴에 연결된 옵션 그룹별로 선택 개수가 minSelect~maxSelect 범위인지 확인.
    // 옵션엔 수량이 없으므로 "그룹에 속한 선택된 옵션 개수"가 곧 해당 그룹에 대한 선택 수임.
    private void validateOptionSelection(UUID menuId, List<UUID> optionItemIds) {
        Map<UUID, OptionGroup> allowedGroupById = menuOptionGroupRepository.findAllByMenuId(menuId).stream()
                .collect(Collectors.toMap(
                        mog -> mog.getOptionGroup().getId(),
                        MenuOptionGroup::getOptionGroup));

        List<OptionItem> selectedOptionItems = optionItemIds.isEmpty()
                ? List.of()
                : optionItemRepository.findAllById(optionItemIds);
        if (selectedOptionItems.size() != optionItemIds.size()) {
            throw new CustomException(ErrorCode.CART_OPTION_ITEM_NOT_FOUND);
        }

        Map<UUID, Long> selectedCountByGroupId = selectedOptionItems.stream()
                .collect(Collectors.groupingBy(item -> item.getOptionGroup().getId(), Collectors.counting()));

        for (UUID groupId : selectedCountByGroupId.keySet()) {
            if (!allowedGroupById.containsKey(groupId)) {
                throw new CustomException(ErrorCode.CART_OPTION_NOT_BELONG_TO_MENU);
            }
        }

        for (OptionGroup group : allowedGroupById.values()) {
            long selectedCount = selectedCountByGroupId.getOrDefault(group.getId(), 0L);
            if (selectedCount < group.getMinSelect() || selectedCount > group.getMaxSelect()) {
                throw new CustomException(ErrorCode.CART_OPTION_GROUP_SELECTION_INVALID);
            }
        }
    }

    @Transactional
    public CartUpdateItemAmountResponseDto updateCartItemAmount(Long userId, UUID cartId, UUID cartItemId, CartUpdateItemAmountRequestDto cartUpdateItemAmountRequestDto) {
        validateCartOwner(userId, cartId);
        CartItem cartItem = cartItemRepository.findByIdAndCart_IdAndDeletedAtIsNull(cartItemId, cartId)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_ITEM_NOT_FOUND));
        cartItem.setQuantity(cartUpdateItemAmountRequestDto.quantity());
        return new CartUpdateItemAmountResponseDto(cartItem.getId(), cartItem.getQuantity());
    }

    @Transactional
    public ApiResponse<Void> deleteCartItem(Long userId, UUID cartId, UUID cartItemId) {
        validateCartOwner(userId, cartId);
        CartItem cartItem = cartItemRepository.findByIdAndCart_IdAndDeletedAtIsNull(cartItemId, cartId)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_ITEM_NOT_FOUND));

        // 딸린 옵션도 함께 soft delete
        List<CartItemOption> options = cartItemOptionRepository.findAllByCartItem(cartItem);
        options.forEach(option -> option.softDelete(userId));

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

    @Transactional
    public CheckoutResponseDto checkout(Long userId, UUID cartId, CheckoutRequestDto requestDto) {
        // validation
        // userId - 카트 주인 확인, delete 상태 확인
        // cart item 최소 하나 이상
        // 금액 계산, 최소 주문 금액
        Cart cart = validateCartOwner(userId, cartId);
        List<CartItem> cartItemList = cartItemRepository.findAllByCart_IdAndDeletedAtIsNull(cartId);
        if(cartItemList.isEmpty())
            throw new CustomException(ErrorCode.ORDER_ITEMS_IS_EMPTY);

        List<CartLineItem> lineItemList = getCartLineItems(cartItemList);

        int totalPrice = lineItemList.stream().mapToInt(lineItem -> {
            int optionPriceSum = lineItem.options.stream().mapToInt(OptionItem::getPrice).sum();
            return (optionPriceSum + lineItem.menu().getPrice()) * lineItem.cartItem().getQuantity();
        }).sum();

        // logic
        // order/orderItem/orderItemOption persist
        // cart softDelete
        // OM 객체 생성
        Orders order = new Orders(
                userId, cart.getStoreId(), cart, OrderStatusEnum.PENDING, totalPrice,
                requestDto.deliveryAddress(), requestDto.requestMessage()
        );
        Orders savedOrder = orderRepository.save(order);

        List<OrderItem> orderItemList = lineItemList.stream()
                .map(lineItem -> new OrderItem(
                        savedOrder,
                        lineItem.menu().getId(),
                        lineItem.menu().getName(),
                        lineItem.menu().getPrice(),
                        lineItem.cartItem().getQuantity()
                )).toList();
        List<OrderItem> savedOrderItemList = orderItemRepository.saveAll(orderItemList);

        List<OrderItemOption> orderItemOptions = new ArrayList<>();
        for (int i = 0; i < lineItemList.size(); i++) {
            OrderItem savedItem = savedOrderItemList.get(i);
            for (OptionItem option : lineItemList.get(i).options()) {
                orderItemOptions.add(new OrderItemOption(savedItem, option.getId(), option.getName(), option.getPrice()));
            }
        }
        orderItemOptionRepository.saveAll(orderItemOptions);

        orderManagementService.create(savedOrder, OrderStatusEnum.PENDING);

        cart.softDelete(userId);
        for (CartItem cartItem : cartItemList) {
            cartItem.softDelete(userId);
        }
        List<CartItemOption> cartItemOptionList = cartItemOptionRepository.findAllByCartItemIn(
                lineItemList.stream().map(CartLineItem::cartItem).toList()
        );
        cartItemOptionList.forEach(option -> option.softDelete(userId));

        return new CheckoutResponseDto(savedOrder, requestDto.deliveryAddress(), requestDto.requestMessage());
    }
}
