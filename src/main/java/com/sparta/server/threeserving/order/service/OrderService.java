package com.sparta.server.threeserving.order.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.order.dto.request.OrderCreateRequestDto;
import com.sparta.server.threeserving.order.dto.request.OrderItemRequestDto;
import com.sparta.server.threeserving.order.dto.request.OrderModifyRequestDto;
import com.sparta.server.threeserving.order.dto.response.*;
import com.sparta.server.threeserving.order.entity.OrderItem;
import com.sparta.server.threeserving.order.entity.OrderItemOption;
import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order.entity.Orders;
import com.sparta.server.threeserving.order.repository.OrderItemOptionRepository;
import com.sparta.server.threeserving.order.repository.OrderItemRepository;
import com.sparta.server.threeserving.order.repository.OrderRepository;
import com.sparta.server.threeserving.order_management.service.OrderManagementService;
import com.sparta.server.threeserving.store.repository.StoreRepository;
import com.sparta.server.threeserving.user.entity.User;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import com.sparta.server.threeserving.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemOptionRepository orderItemOptionRepository;

    private final OrderManagementService orderManagementService;

    private final StoreRepository storeRepository;

    private static final List<Integer> ALLOWED_SIZE = List.of(10, 30, 50);
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "totalPrice");
    private static final int AVAILABLE_ORDER_CANCEL_TiME_IN_MINUTES = 5;
    private final UserRepository userRepository;

    @Transactional
    public OrderCreateResponseDto createOrder(OrderCreateRequestDto requestDto) {
        // validation
        userRepository.findById(requestDto.userId()).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        storeRepository.findById(requestDto.storeId()).orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
        if(requestDto.orderItems().isEmpty()) throw new CustomException(ErrorCode.ORDER_ITEMS_IS_EMPTY);

        // 로직 수행
        OrderStatusEnum status = requestDto.orderStatus() == null ? OrderStatusEnum.PENDING : requestDto.orderStatus();
        Orders order = new Orders(
                requestDto.userId(), requestDto.storeId(), null,
                status, requestDto.totalPrice(),
                requestDto.deliveryAddress(), requestDto.requestMessage());
        Orders savedOrder = orderRepository.save(order);

        List<OrderItemRequestDto> itemRequestDtoList = requestDto.orderItems();
        List<OrderItem> orderItemList = itemRequestDtoList.stream().map(dto ->
                new OrderItem(
                        savedOrder, dto.menuId(), dto.menuName(),
                        dto.price(), dto.quantity()
                )
        ).toList();
        List<OrderItem> savedOrderItems = orderItemRepository.saveAll(orderItemList);

        List<OrderItemOption> orderItemOptionList = new ArrayList<>();
        for (int i = 0; i < itemRequestDtoList.size(); i++) {
            OrderItem savedItem = savedOrderItems.get(i);
            OrderItemRequestDto dto = itemRequestDtoList.get(i);

            List<OrderItemOption> options = dto.options().stream()
                    .map(optDto -> new OrderItemOption(
                            savedItem, optDto.optionItemId(), optDto.optionName(),
                            optDto.additionalPrice()))
                    .toList();

            orderItemOptionList.addAll(options);
        }
        orderItemOptionRepository.saveAll(orderItemOptionList);

        orderManagementService.create(savedOrder, status);
        return new OrderCreateResponseDto(savedOrder);
    }

    public OrderDetailResponseDto getOrderDetail(Long userId, UserRoleEnum userRole, UUID orderId) {
        // userId - orderId 접근 확인
        Orders order = switch (userRole) {
            case CUSTOMER -> validateOrderOwner(userId, orderId);
            case OWNER -> {
                List<UUID> storeIdList = storeRepository.findStoreIdsByOwnerId(userId);
                Orders uncheckedOrder = orderRepository.findByIdAndDeletedAtIsNull(orderId).orElseThrow(
                        () -> new CustomException(ErrorCode.ORDER_NOT_FOUND)
                );
                if (!storeIdList.contains(uncheckedOrder.getStoreId())) {
                    throw new CustomException(ErrorCode.NOT_STORE_OWNER_OF_ORDER);
                }
                yield uncheckedOrder;
            }
            case MANAGER, MASTER -> orderRepository.findByIdAndDeletedAtIsNull(orderId).orElseThrow(
                    () -> new CustomException(ErrorCode.ORDER_NOT_FOUND)
            );
        };

        // Order 목록 작성
        List<OrderItem> orderItemList = orderItemRepository.findAllByOrderAndDeletedAtIsNull(order);

        Map<UUID, List<OrderItemOption>> optionByOrderItem = orderItemOptionRepository.findAllByOrderItemIn(orderItemList).stream()
                .collect(Collectors.groupingBy(option -> option.getOrderItem().getId()));

        List<OrderItemResponseDto> items = orderItemList.stream()
                .map(item -> new OrderItemResponseDto(
                        item,
                        optionByOrderItem.getOrDefault(item.getId(), List.of()).stream()
                                .map(option -> new OrderItemOptionResponseDto(
                                        option.getOptionName(), option.getAdditionalPrice()
                                )).toList()
                )).toList();

        return new OrderDetailResponseDto(order, items);
    }

    public Page<OrderListResponseDto> getOrderList(User user, UUID storeId, Long targetUserID, OrderStatusEnum orderStatusEnum, int size, int page, String sortBy, boolean isAsc) {
        Pageable pageable = toPageable(size, page, sortBy, isAsc);
        Page<Orders> orderPage = switch (user.getRole()) {
            case CUSTOMER -> getForCustomer(user.getId(), storeId, orderStatusEnum, pageable);
            case OWNER -> getForOwner(user.getId(), storeId, orderStatusEnum, pageable);
            case MANAGER, MASTER -> getForAdmin(targetUserID, storeId, orderStatusEnum, pageable);
        };

        return orderPage.map(OrderListResponseDto::new);
    }

    private Page<Orders> getForAdmin(Long userId, UUID storeId, OrderStatusEnum status, Pageable pageable) {
        if(userId != null){
            return getForCustomer(userId, storeId, status, pageable);
        }
        if (storeId != null) {
            return (status != null)
                    ? orderRepository.findByStoreIdAndOrderStatusAndDeletedAtIsNull(storeId, status, pageable)
                    : orderRepository.findByStoreIdAndDeletedAtIsNull(storeId, pageable);
        }
        return (status != null)
                ? orderRepository.findByOrderStatusAndDeletedAtIsNull(status, pageable)
                : orderRepository.findByDeletedAtIsNull(pageable);
    }

    private Page<Orders> getForOwner(Long ownerId, UUID storeId, OrderStatusEnum status, Pageable pageable) {
        List<UUID> ownedStoreIdList = storeRepository.findStoreIdsByOwnerId(ownerId);

        if(storeId != null) {
            if(!ownedStoreIdList.contains(storeId)) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }
            return (status != null)
                    ? orderRepository.findByStoreIdAndOrderStatusAndDeletedAtIsNull(storeId, status, pageable)
                    : orderRepository.findByStoreIdAndDeletedAtIsNull(storeId, pageable);
        }
        return (status != null)
                ? orderRepository.findByStoreIdInAndOrderStatusAndDeletedAtIsNull(ownedStoreIdList, status, pageable)
                : orderRepository.findByStoreIdInAndDeletedAtIsNull(ownedStoreIdList, pageable);
    }

    private Page<Orders> getForCustomer(Long userId, UUID storeId, OrderStatusEnum status, Pageable pageable) {
        if(storeId != null)
            return (status != null)
                    ? orderRepository.findAllByUserIdAndStoreIdAndOrderStatusAndDeletedAtIsNull(userId, storeId, status, pageable)
                    : orderRepository.findAllByUserIdAndStoreIdAndDeletedAtIsNull(userId, storeId, pageable);
        return (status != null)
            ? orderRepository.findAllByUserIdAndOrderStatusAndDeletedAtIsNull(userId, status, pageable)
            : orderRepository.findAllByUserIdAndDeletedAtIsNull(userId, pageable);
    }

    private Pageable toPageable(int size, int page, String sortBy, boolean isAsc) {
        sortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort.Direction dir = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(dir, sortBy);
        size = ALLOWED_SIZE.contains(size) ? size : 10;
        return PageRequest.of(page, size, sort);
    }

    @Transactional
    public OrderModifyResponseDto modifyOrderInfo(Long userId, UUID orderId, OrderModifyRequestDto orderModifyRequestDto) {
        Orders order = validateOrderOwner(userId, orderId);

        if (order.getOrderStatus() != OrderStatusEnum.PENDING) {
            throw new CustomException(ErrorCode.ORDER_ALREADY_PROCESSED);
        }

        order.modifyInfo(
                orderModifyRequestDto.requestMessage(),
                orderModifyRequestDto.deliveryAddress()
        );

        return new OrderModifyResponseDto(order);
    }

    @Transactional
    public OrderCancelResponseDto cancelOrder(Long userId, UUID orderId) {
        Orders order = validateOrderOwner(userId, orderId);

        if(order.getOrderStatus() != OrderStatusEnum.PENDING)
            throw new CustomException(ErrorCode.ORDER_ALREADY_PROCESSED);

        Duration elapsed = Duration.between(order.getCreatedAt(), Instant.now());

        if(elapsed.compareTo(Duration.ofMinutes(AVAILABLE_ORDER_CANCEL_TiME_IN_MINUTES)) > 0){
            throw new CustomException(ErrorCode.EXPIRED_CANCEL_TIME);
        }

        orderManagementService.cancelOrderAndHistory(order);

        return new OrderCancelResponseDto(order);
    }

    @Transactional
    public ApiResponse<Void> deleteOrder(Long userId, UUID orderId) {
        // MANAGER 전용 api이므로 owner로직 적용x
        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        List<OrderItem> orderItemList = orderItemRepository.findAllByOrderAndDeletedAtIsNull(order);

        // p_order_item_option 테이블엔 deleted_at 컬럼이 없어 soft delete 불가 -> 딸린 옵션은 하드 삭제
        if(!orderItemList.isEmpty()) {
            List<OrderItemOption> options = orderItemOptionRepository.findAllByOrderItemIn(orderItemList);
            orderItemOptionRepository.deleteAll(options);
        }

        for (OrderItem orderItem : orderItemList) {
            orderItem.softDelete(userId);
        }
        order.softDelete(userId);
        return ApiResponse.success(SuccessCode.DELETED);
    }

    @NonNull
    private Orders validateOrderOwner(Long userId, UUID orderId) {
        Orders order = orderRepository.findByIdAndDeletedAtIsNull(orderId).orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
        if(!order.getUserId().equals(userId))
            throw new CustomException(ErrorCode.NOT_ORDER_OWNER);
        return order;
    }


}