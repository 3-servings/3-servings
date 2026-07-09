package com.sparta.server.threeserving.order.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.order.dto.request.OrderCreateRequestDto;
import com.sparta.server.threeserving.order.dto.request.OrderItemRequestDto;
import com.sparta.server.threeserving.order.dto.response.OrderCreateResponseDto;
import com.sparta.server.threeserving.order.dto.response.OrderDetailResponseDto;
import com.sparta.server.threeserving.order.dto.response.OrderItemOptionResponseDto;
import com.sparta.server.threeserving.order.dto.response.OrderItemResponseDto;
import com.sparta.server.threeserving.order.entity.OrderItem;
import com.sparta.server.threeserving.order.entity.OrderItemOption;
import com.sparta.server.threeserving.order.entity.Orders;
import com.sparta.server.threeserving.order.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemOptionRepository cartItemOptionRepository;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemOptionRepository orderItemOptionRepository;

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
}
