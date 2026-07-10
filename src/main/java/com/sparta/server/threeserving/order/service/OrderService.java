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
import com.sparta.server.threeserving.order.repository.*;
import com.sparta.server.threeserving.store.repository.StoreRepository;
import com.sparta.server.threeserving.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemOptionRepository cartItemOptionRepository;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemOptionRepository orderItemOptionRepository;

    private final StoreRepository storeRepository;

    private static final List<Integer> ALLOWED_SIZE = List.of(10, 30, 50);
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "totalPrice");


    @Transactional
    public ApiResponse<OrderCreateResponseDto> createOrder(OrderCreateRequestDto requestDto) {
        Orders order = new Orders(
                requestDto.userId(), requestDto.storeId(), null,
                requestDto.orderStatus(), requestDto.totalPrice(),
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
                            optDto.additionalPrice(), 1))
                    .toList();

            orderItemOptionList.addAll(options);
        }
        orderItemOptionRepository.saveAll(orderItemOptionList);

        return ApiResponse.success(SuccessCode.CREATED, new OrderCreateResponseDto(savedOrder));
    }

    public ApiResponse<OrderDetailResponseDto> getOrderDetail(Long userId, UUID orderId) {
        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
        List<OrderItem> orderItemList = orderItemRepository.findAllByOrder_IdAndDeletedAtIsNull(order);

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

        return ApiResponse.success(SuccessCode.CREATED, new OrderDetailResponseDto(order, items));
    }

    // TODO: 언젠가 QueryDSL로 변경해도 될듯.
    public ApiResponse<Page<OrderListResponseDto>> getOrderList(User user, UUID storeId, Long userId, OrderStatusEnum orderStatusEnum, int size, int page, String sortBy, boolean isAsc) {
        Pageable pageable = toPageable(size, page, sortBy, isAsc);
        Page<Orders> orderPage = switch (user.getRole()) {
            case CUSTOMER -> getForCustomer(user.getId(), storeId, orderStatusEnum, pageable);
            case OWNER -> getForOwner(user.getId(), storeId, orderStatusEnum, pageable);
            case MANAGER, MASTER -> getForAdmin(storeId, orderStatusEnum, pageable);
        };

        Page<OrderListResponseDto> responsePage = orderPage.map(OrderListResponseDto::new);
        return ApiResponse.success(SuccessCode.SUCCESS, responsePage);
    }

    private Page<Orders> getForAdmin(UUID storeId, OrderStatusEnum status, Pageable pageable) {
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
                // TODO: 에러코드 더 생기면 바꾸기
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
        sortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdBy";
        Sort.Direction dir = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(dir, sortBy);
        size = ALLOWED_SIZE.contains(size) ? size : 10;
        return PageRequest.of(page, size, sort);
    }

    @Transactional
    public ApiResponse<OrderModifyResponseDto> modifyOrderInfo(Long userId, UUID orderId, OrderModifyRequestDto orderModifyRequestDto) {
        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
        if(!order.getUserId().equals(userId))
            throw new CustomException(ErrorCode.NOT_ORDER_OWNER);

        order.modifyInfo(
                orderModifyRequestDto.requestMessage(),
                orderModifyRequestDto.deliveryAddress()
        );

        OrderModifyResponseDto responseDto = new OrderModifyResponseDto(order);
        return ApiResponse.success(SuccessCode.SUCCESS, responseDto);
    }
}