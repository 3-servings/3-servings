package com.sparta.server.threeserving.ordermanagement.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order.entity.Orders;
import com.sparta.server.threeserving.order_management.dto.OrderManagementResponse;
import com.sparta.server.threeserving.order_management.entity.OrderManagement;
import com.sparta.server.threeserving.order_management.entity.OrderStatusHistory;
import com.sparta.server.threeserving.order_management.entity.RejectReasonCode;
import com.sparta.server.threeserving.order_management.repository.OrderManagementRepository;
import com.sparta.server.threeserving.order_management.repository.OrderStatusHistoryRepository;
import com.sparta.server.threeserving.order_management.repository.RejectReasonCodeRepository;
import com.sparta.server.threeserving.order_management.service.OrderManagementService;
import com.sparta.server.threeserving.store.entity.Store;
import jakarta.persistence.EntityManager;
import lombok.Builder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdermasterTest {


        @Mock
        private OrderManagementRepository orderManagementRepository;

        @InjectMocks
        private OrderManagementService orderManagementService;

        @Mock
        private RejectReasonCodeRepository rejectReasonCodeRepository;

        @Mock
        private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Test
    void 주문상태_상세조회_성공() {

        UUID orderManagementId = UUID.randomUUID();

        // 가짜 OrderManagement 생성
        OrderManagement orderManagement = mock(OrderManagement.class);

        // Mock 객체가 반환할 데이터 설정
        given(orderManagement.getId())
                .willReturn(orderManagementId);

        given(orderManagement.getOrderStatusEnum())
                .willReturn(OrderStatusEnum.PENDING);

        given(orderManagement.getEstimatedCookTime())
                .willReturn(30);


        // Repository가 위의 데이터를 반환하도록 설정
        given(orderManagementRepository.findById(orderManagementId))
                .willReturn(Optional.of(orderManagement));


        // Service 실행
        OrderManagementResponse response =
                orderManagementService.getOrderManagementDetail(orderManagementId);


        // 결과 확인
        System.out.println("ID : " + response.getOrderManagementId());
        System.out.println("STATUS : " + response.getOrderStatus());
        System.out.println("TIME : " + response.getEstimatedCookTime());


        assertEquals(orderManagementId, response.getOrderManagementId());
    }

    @Test
    void 주문거절_성공() {

        // given
        UUID orderManagementId = UUID.randomUUID();
        UUID rejectReasonCodeId = UUID.randomUUID();

        Orders order = mock(Orders.class);
        RejectReasonCode rejectReasonCode = mock(RejectReasonCode.class);
        OrderManagement orderManagement = mock(OrderManagement.class);

        when(orderManagementRepository.findById(orderManagementId))
                .thenReturn(Optional.of(orderManagement));

        when(rejectReasonCodeRepository.findById(rejectReasonCodeId))
                .thenReturn(Optional.of(rejectReasonCode));

        when(orderManagement.getOrderStatusEnum())
                .thenReturn(OrderStatusEnum.PENDING);


        when(orderManagement.getOrders())
                .thenReturn(order);

        // when
        orderManagementService.rejectOrder(
                orderManagementId,
                rejectReasonCodeId,
                "재료 소진"
        );
        // then
        verify(orderManagement).reject(rejectReasonCode, "재료 소진");
        verify(order).changeStatus(OrderStatusEnum.REJECTED);
        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));

    }


    @Test
    @DisplayName("상태 변경 제약 테스트")
    void reject_fail_not_pending() {

        // given
        Orders order = new Orders();
        Store store = mock(Store.class);

        RejectReasonCode rejectReasonCode = new RejectReasonCode();
        rejectReasonCode.setCode("OUT_OF_STOCK");
        rejectReasonCode.setDescription("재료 소진");

        OrderManagement orderManagement = new OrderManagement(order, store, OrderStatusEnum.ACCEPTED);

        // PENDING -> ACCEPTED
        orderManagement.changeStatus(OrderStatusEnum.COOKING);

        // when & then
        assertThatThrownBy(() ->
                orderManagement.reject(rejectReasonCode, "재료 소진")
        )
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.ORDER_STATUS_TRANSITION_INVALID.getMessage());
    }
}
