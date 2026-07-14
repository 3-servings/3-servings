package com.sparta.server.threeserving.order_management.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order.entity.Orders;
import com.sparta.server.threeserving.order.repository.OrderRepository;
import com.sparta.server.threeserving.order_management.dto.request.OrderManagementCreateRequest;
import com.sparta.server.threeserving.order_management.dto.response.OrderManagementListResponse;
import com.sparta.server.threeserving.order_management.dto.response.OrderManagementResponse;
import com.sparta.server.threeserving.order_management.dto.response.OrderStatusHistoryResponse;
import com.sparta.server.threeserving.order_management.entity.OrderManagement;
import com.sparta.server.threeserving.order_management.entity.OrderStatusHistory;
import com.sparta.server.threeserving.order_management.entity.RejectReasonCode;
import com.sparta.server.threeserving.order_management.repository.OrderManagementRepository;
import com.sparta.server.threeserving.order_management.repository.OrderStatusHistoryRepository;
import com.sparta.server.threeserving.order_management.repository.RejectReasonCodeRepository;
import com.sparta.server.threeserving.order_management.validator.StoreAccessValidator;
import com.sparta.server.threeserving.store.entity.Store;
import com.sparta.server.threeserving.store.repository.StoreRepository;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderManagementService {

// TODO: Role(MASTER/OWNER)에 따른 Store 접근 권한 체크 추가

    private final OrderManagementRepository orderManagementRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final RejectReasonCodeRepository rejectReasonCodeRepository;
    private final OrderRepository orderRepository;
    private final EntityManager entityManager;
    private final StoreRepository storeRepository;
    private final StoreAccessValidator storeAccessValidator;

// Payment 성공 시 호출
    @Transactional
    public OrderManagement create(OrderManagementCreateRequest request) {
        Orders order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("주문 없음"));
        Store store = entityManager.getReference(
                Store.class,
                order.getStoreId()
        );
        OrderManagement orderManagement =
                new OrderManagement(order,store,OrderStatusEnum.PENDING);

        return orderManagementRepository.save(orderManagement);
    }

// Cart에서 체크아웃, 혹은 MASTER 강제 생성 시 호출
    @Transactional
    public OrderManagement create(Orders order, OrderStatusEnum initialStatus) {
        Store store = entityManager.getReference(
                Store.class,
                order.getStoreId()
        );
        OrderManagement orderManagement =
                new OrderManagement(order,store,initialStatus);

        return orderManagementRepository.save(orderManagement);
    }

    public Page<OrderManagementListResponse> getOrderManagementList(UUID storeId, OrderStatusEnum status, Pageable pageable, Long userId, UserRoleEnum role) {

        if (role == UserRoleEnum.OWNER) {
            storeAccessValidator.validateStoreAccess(userId, storeId);
        }
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
    public void cancelOrderAndHistory(Orders order){
        OrderManagement orderManagement =orderManagementRepository.findByOrders(order)
                        .orElseThrow(() ->
                                new CustomException(ErrorCode.ORDER_MANAGEMENT_NOT_FOUND)
                        );

        OrderStatusEnum previousStatus = orderManagement.getOrderStatus();
        OrderStatusEnum status = OrderStatusEnum.CANCELED;

        // 1. 주문 관리 상태 변경
        orderManagement.changeStatus(status);

        // 2. 주문 상태 변경, 상태 이력 저장
        updateOrderAndHistory(orderManagement,previousStatus,status);


    }
    @Transactional
    public void acceptOrder(UUID orderManagementId,Integer estimatedCookTime) {

        OrderManagement orderManagement = getOrderManagement(orderManagementId);
        OrderStatusEnum previousStatus = orderManagement.getOrderStatus();

        // 1. 주문 관리 상태 변경
        orderManagement.accept(estimatedCookTime);

        // 2. 주문 상태 변경, 상태 이력 저장
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

        // 2. 주문 상태 변경, 상태 이력 저장
        updateOrderAndHistory(orderManagement,previousStatus,OrderStatusEnum.REJECTED);

    }

    @Transactional
    public void updateStatus(UUID orderManagementId, @NotNull OrderStatusEnum status) {

        OrderManagement orderManagement = getOrderManagement(orderManagementId);
        OrderStatusEnum previousStatus = orderManagement.getOrderStatus();

        // 1. 주문 관리 상태 변경
        orderManagement.changeStatus(status);

        // 2. 주문 상태 변경, 상태 이력 저장
        updateOrderAndHistory(orderManagement,previousStatus,status);

        if (status == OrderStatusEnum.COMPLETED) {
            orderManagement.getStore().updateOrderCnt();
        }

    }

    @Transactional
    public void updateCookingTime(UUID orderManagementId, Integer estimatedCookTime) {

        OrderManagement orderManagement = getOrderManagement(orderManagementId);
        OrderStatusEnum status = orderManagement.getOrderStatus();
        if (status != OrderStatusEnum.ACCEPTED &&
                status != OrderStatusEnum.COOKING) {
            throw new CustomException(ErrorCode.ORDER_STATUS_TRANSITION_INVALID);
        }

        orderManagement.changeCookingTime(estimatedCookTime);

    }

    @Transactional
    public OrderStatusHistoryResponse getOrderStatusHistory(UUID orderManagementId) {

        getOrderManagement(orderManagementId);

//        List<OrderStatusHistory> histories =
//                orderStatusHistoryRepository.findByOrderManagementIdOrderByCreatedAtAsc(orderManagementId);


        List<OrderStatusHistoryResponse.History> history =
                orderStatusHistoryRepository
                        .findByOrderManagementIdOrderByCreatedAtAsc(orderManagementId)
                        .stream()
                        .map(h -> new OrderStatusHistoryResponse.History(
                                h.getPreviousStatus(),
                                h.getCurrentStatus(),
                                h.getMemo(),
                                h.getCreatedAt()
                        ))
                        .toList();

        return new OrderStatusHistoryResponse(orderManagementId, history);
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



