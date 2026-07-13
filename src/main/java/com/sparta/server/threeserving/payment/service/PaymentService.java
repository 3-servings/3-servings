package com.sparta.server.threeserving.payment.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.order.entity.Orders;
import com.sparta.server.threeserving.order.repository.OrderRepository;
import com.sparta.server.threeserving.order_management.dto.request.OrderManagementCreateRequest;
import com.sparta.server.threeserving.order_management.service.OrderManagementService;
import com.sparta.server.threeserving.payment.dto.request.PaymentRequest;
import com.sparta.server.threeserving.payment.dto.response.PaymentLogResponse;
import com.sparta.server.threeserving.payment.dto.response.PaymentResponse;
import com.sparta.server.threeserving.payment.dto.response.RefundSuccessResponse;
import com.sparta.server.threeserving.payment.entity.Payment;
import com.sparta.server.threeserving.payment.entity.PaymentLog;
import com.sparta.server.threeserving.payment.enums.PaymentStatus;
import com.sparta.server.threeserving.payment.repository.PaymentLogRepository;
import com.sparta.server.threeserving.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentLogRepository paymentLogRepository;
    private final OrderRepository orderRepository;
    private final OrderManagementService orderManagementService;

    private Orders validateOrder(Long userId, UUID orderId){
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if(!order.getUserId().equals(userId)){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        return order;
    }

    private Payment findPayment(UUID orderId){
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    public PaymentResponse createPayment(Long userId, UUID orderId, PaymentRequest request){

        Orders order = validateOrder(userId, orderId);

        if(paymentRepository.findByOrderId(orderId).isPresent()){
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        Payment payment = Payment.create(order, request);

        Payment savedPayment = paymentRepository.save(payment);

        paymentLogRepository.save(
                PaymentLog.create(
                        savedPayment,
                        PaymentStatus.SUCCESS,
                        "결제 완료"
                )
        );

        orderManagementService.create(
                new OrderManagementCreateRequest(savedPayment.getOrder().getId())
        );

        return PaymentResponse.from(savedPayment);
    }

    public RefundSuccessResponse refund (Long userId, UUID orderId){

        validateOrder(userId, orderId);
        Payment payment = findPayment(orderId);

        if(payment.getStatus() == PaymentStatus.REFUNDED){
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_REFUNDED);
        }

        if(Duration.between(payment.getApprovedAt(), Instant.now()).toMinutes() >= 5){
            throw new CustomException(ErrorCode.REFUND_EXPIRED);
        }

        paymentLogRepository.save(
                PaymentLog.create(
                        payment,
                        PaymentStatus.REFUND_REQUESTED,
                        "환불 요청"
                )
        );

        payment.refund();

        paymentLogRepository.save(
                PaymentLog.create(
                        payment,
                        PaymentStatus.REFUNDED,
                        "환불 완료"
                )
        );

        return RefundSuccessResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long userId, UUID orderId){

        validateOrder(userId, orderId);
        Payment payment = findPayment(orderId);

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentLogResponse> getPaymentLogs(Long userId, UUID orderId){

        validateOrder(userId, orderId);

        List<PaymentLogResponse> responseList = new ArrayList<>();
        Payment payment = findPayment(orderId);

        List<PaymentLog> paymentLogs = paymentLogRepository.findAllByPayment(payment);

        for(PaymentLog paymentLog : paymentLogs){
            responseList.add(PaymentLogResponse.from(paymentLog));
        }

        return responseList;
    }
}
