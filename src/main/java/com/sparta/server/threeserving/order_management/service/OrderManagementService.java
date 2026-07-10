package com.sparta.server.threeserving.order_management.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order.entity.Orders;
import com.sparta.server.threeserving.order_management.dto.response.OrderManagementListResponse;
import com.sparta.server.threeserving.order_management.dto.response.OrderManagementResponse;
import com.sparta.server.threeserving.order_management.entity.OrderManagement;
import com.sparta.server.threeserving.order_management.entity.OrderStatusHistory;
import com.sparta.server.threeserving.order_management.entity.RejectReasonCode;
import com.sparta.server.threeserving.order_management.repository.OrderManagementRepository;
import com.sparta.server.threeserving.order_management.repository.OrderStatusHistoryRepository;
import com.sparta.server.threeserving.order_management.repository.RejectReasonCodeRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderManagementService {

    private OrderManagementRepository orderManagementRepository;
    private OrderStatusHistoryRepository orderStatusHistoryRepository;
    private RejectReasonCodeRepository rejectReasonCodeRepository;


    // TODO
// Payment 성공 시 호출
//    @Transactional
//    public OrderManagement create(OrderManagementCreateRequest request) {
//
//        Orders order = orderRepository.findById(request.getOrderId())
//                .orElseThrow(() -> new IllegalArgumentException("주문 없음"));
//
//        Store store = order.getStore();
//
//        OrderManagement orderManagement =
//                new OrderManagement(order, store);
//
//        return orderManagementRepository.save(orderManagement);
//        return null;
//    }

    public Page<OrderManagementListResponse> getOrderManagementList(UUID storeId, OrderStatusEnum status, Pageable pageable) {

        if (status == null) {
            return orderManagementRepository
                        .findByStoreId(storeId, pageable)
                        .map(OrderManagementListResponse::new);
            }
            return orderManagementRepository
                    .findByStoreIdAndOrderStatus(storeId, status, pageable)
                    .map(OrderManagementListResponse::new);

        }

    public OrderManagementResponse getOrderManagementDetail(UUID orderManagementId) {

        OrderManagement orderManagement = getOrderManagement(orderManagementId);

        return new OrderManagementResponse(orderManagement);
    }


    @Transactional
    public void acceptOrder(UUID orderManagementId,Integer estimatedCookTime) {

        OrderManagement orderManagement = getOrderManagement(orderManagementId);
        OrderStatusEnum previousStatus = orderManagement.getOrderStatus();

        // 1. 주문 관리 상태 변경
        orderManagement.accept(estimatedCookTime);

        // 2. 상태 이력 저장
        updateOrderAndHistory(orderManagement,previousStatus,OrderStatusEnum.ACCEPTED);

    }


    @Transactional
    public void rejectOrder(UUID orderManagementId,UUID rejectReasonCodeId, String memo) {

        OrderManagement orderManagement = getOrderManagement(orderManagementId);
        RejectReasonCode rejectReasonCode = rejectReasonCodeRepository.findById(rejectReasonCodeId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.REJECT_MEMO_REQUIRED
                        ));

        OrderStatusEnum previousStatus = orderManagement.getOrderStatus();

        // 1. 주문 관리 상태 변경
        orderManagement.reject(rejectReasonCode,memo);

        // 2. 상태 이력 저장
        updateOrderAndHistory(orderManagement,previousStatus,OrderStatusEnum.REJECTED);

    }

    @Transactional
    public void updateStatus(UUID orderManagementId, @NotNull OrderStatusEnum status) {

        OrderManagement orderManagement = getOrderManagement(orderManagementId);
        OrderStatusEnum previousStatus = orderManagement.getOrderStatus();

        // 1. 주문 관리 상태 변경
        orderManagement.changeStatus(status);

        // 2. 상태 이력 저장
        updateOrderAndHistory(orderManagement,previousStatus,status);

    }


    //공통 메서드
    //OrderManagement 조회
    private OrderManagement getOrderManagement(UUID orderManagementId) {

        return orderManagementRepository.findById(orderManagementId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.ORDER_MANAGEMENT_NOT_FOUND));
    }


    //주문 내역 히스토리 저장
    private void updateOrderAndHistory(
            OrderManagement orderManagement,
            OrderStatusEnum previousStatus,
            OrderStatusEnum currentStatus
    ) {

        Orders order = orderManagement.getOrders();
        order.changeStatus(currentStatus);

        orderStatusHistoryRepository.save(
                new OrderStatusHistory(
                        orderManagement,
                        previousStatus,
                        currentStatus
                )
        );
    }
}



