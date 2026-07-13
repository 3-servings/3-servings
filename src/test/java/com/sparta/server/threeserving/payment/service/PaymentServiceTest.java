package com.sparta.server.threeserving.payment.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.order.entity.Orders;
import com.sparta.server.threeserving.order.repository.OrderRepository;
import com.sparta.server.threeserving.order_management.dto.request.OrderManagementCreateRequest;
import com.sparta.server.threeserving.order_management.entity.OrderManagement;
import com.sparta.server.threeserving.order_management.service.OrderManagementService;
import com.sparta.server.threeserving.payment.dto.request.PaymentRequest;
import com.sparta.server.threeserving.payment.dto.response.PaymentLogResponse;
import com.sparta.server.threeserving.payment.dto.response.PaymentResponse;
import com.sparta.server.threeserving.payment.dto.response.RefundSuccessResponse;
import com.sparta.server.threeserving.payment.entity.Payment;
import com.sparta.server.threeserving.payment.entity.PaymentLog;
import com.sparta.server.threeserving.payment.enums.PaymentMethod;
import com.sparta.server.threeserving.payment.enums.PaymentStatus;
import com.sparta.server.threeserving.payment.repository.PaymentLogRepository;
import com.sparta.server.threeserving.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("paymentservice-unit")
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentLogRepository paymentLogRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderManagementService orderManagementService;

    private Long userId;
    private UUID orderId;
    private Orders order;
    private Payment payment;

    @BeforeEach
    void setUp(){
        userId = 1L;
        orderId = UUID.randomUUID();

        order = new Orders();
        order.setId(orderId);
        order.setUserId(userId);
        order.setTotalPrice(10000);

        payment = Payment.builder()
                .order(order)
                .paymentMethod(PaymentMethod.CARD)
                .amount(10000L)
                .transactionId("TXN-001")
                .status(PaymentStatus.SUCCESS)
                .requestedAt(Instant.now())
                .approvedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("결제 생성 성공")
    void createPaymentSuccess(){
        PaymentRequest request = new PaymentRequest(PaymentMethod.CARD);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.empty());

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(orderManagementService.create(any(OrderManagementCreateRequest.class)))
                .thenReturn(mock(OrderManagement.class));

        PaymentResponse response = paymentService.createPayment(userId, orderId, request);

        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);

        verify(paymentRepository).save(any(Payment.class));
        verify(paymentLogRepository).save(any(PaymentLog.class));
        verify(orderManagementService).create(any(OrderManagementCreateRequest.class));
    }

    @Test
    @DisplayName("이미 결제된 주문")
    void createPaymentAlreadyExists() {
        PaymentRequest request = new PaymentRequest(PaymentMethod.CARD);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.of(payment));

        CustomException exception = assertThrows(
                CustomException.class,
                () -> paymentService.createPayment(userId, orderId, request)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.PAYMENT_ALREADY_EXISTS);

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verify(paymentLogRepository, never())
                .save(any(PaymentLog.class));
    }

    @Test
    @DisplayName("다른 사용자의 주문으로 결제 생성 실패")
    void createPaymentAccessDenied(){
        PaymentRequest request = new PaymentRequest(PaymentMethod.CARD);

        order.setUserId(2L);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        CustomException exception = assertThrows(
                CustomException.class,
                () -> paymentService.createPayment(userId, orderId, request)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.ACCESS_DENIED);

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verify(paymentLogRepository, never())
                .save(any(PaymentLog.class));
    }

    @Test
    @DisplayName("주문 없음")
    void createPaymentOrderNotFound() {
        PaymentRequest request = new PaymentRequest(PaymentMethod.CARD);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        CustomException exception = assertThrows(
                CustomException.class,
                ()->paymentService.createPayment(userId, orderId, request)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

        verify(paymentRepository, never()).save(any());
        verify(paymentLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("환불 성공")
    void refundSuccess() {
        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.of(payment));

        RefundSuccessResponse response = paymentService.refund(userId, orderId);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(payment.getRefundAt()).isNotNull();

        verify(paymentLogRepository, times(2))
                .save(any(PaymentLog.class));
    }

    @Test
    @DisplayName("이미 환불된 결제")
    void alreadyRefunded(){
        payment.refund();

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.of(payment));

        CustomException exception = assertThrows(
                CustomException.class,
                () -> paymentService.refund(userId, orderId)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.PAYMENT_ALREADY_REFUNDED);

        verify(paymentLogRepository, never())
                .save(any());
    }

    @Test
    @DisplayName("환불 가능 시간 초과")
    void refundExpired(){
        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        payment = Payment.builder()
                .order(order)
                .paymentMethod(PaymentMethod.CARD)
                .amount(10000L)
                .transactionId("TXN-001")
                .status(PaymentStatus.SUCCESS)
                .requestedAt(Instant.now().minus(Duration.ofMinutes(6)))
                .approvedAt(Instant.now().minus(Duration.ofMinutes(6)))
                .build();

        when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.of(payment));

        CustomException exception = assertThrows(
                CustomException.class,
                () -> paymentService.refund(userId, orderId)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.REFUND_EXPIRED);

        verify(paymentLogRepository, never())
                .save(any());
    }

    @Test
    @DisplayName("결제 정보를 찾을 수 없는 경우")
    void paymentNotFound(){
        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.empty());

        CustomException exception = assertThrows(
                CustomException.class,
                ()->paymentService.refund(userId, orderId)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.PAYMENT_NOT_FOUND);

        verify(paymentLogRepository, never())
                .save(any());
    }

    @Test
    @DisplayName("결제 조회 성공")
    void getPaymentSuccess(){
        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPayment(userId, orderId);

        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(response.getTransactionId()).isEqualTo("TXN-001");

        verify(paymentRepository, times(1))
                .findByOrderId(orderId);
    }

    @Test
    @DisplayName("결제 조회 실패 - 결제 정보 없음")
    void getPaymentNotFound() {
        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.empty());

        CustomException exception = assertThrows(
                CustomException.class,
                () -> paymentService.getPayment(userId, orderId)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.PAYMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("결제 로그 조회 성공")
    void getPaymentLogsSuccess() {
        PaymentLog paymentLog1 = PaymentLog.create(
                payment,
                PaymentStatus.SUCCESS,
                "결제 완료"
        );

        PaymentLog paymentLog2 = PaymentLog.create(
                payment,
                PaymentStatus.REFUNDED,
                "환불 완료"
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.of(payment));

        when(paymentLogRepository.findAllByPayment(payment))
                .thenReturn(List.of(paymentLog1, paymentLog2));

        List<PaymentLogResponse> response = paymentService.getPaymentLogs(userId, orderId);

        assertThat(response).hasSize(2);

        assertThat(response.get(0).getStatus())
                .isEqualTo(PaymentStatus.SUCCESS);

        assertThat(response.get(1).getStatus())
                .isEqualTo(PaymentStatus.REFUNDED);

        verify(paymentLogRepository, times(1))
                .findAllByPayment(payment);
    }

    @Test
    @DisplayName("결제 로그 조회 실패 - 결제 정보 없음")
    void getPaymentLogsNotFound(){
        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.empty());

        CustomException exception = assertThrows(
                CustomException.class,
                () -> paymentService.getPaymentLogs(userId, orderId)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.PAYMENT_NOT_FOUND);
    }
}