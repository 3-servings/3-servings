package com.sparta.server.threeserving.payment.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.order.entity.Orders;
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
    //Todo
    //private final OrderRepository orderRepository;

    public PaymentResponse createPayment(UUID orderId, PaymentRequest request){
        //Todo order 조회
        //Orders order = orderRepository.findById(orderId).orElseThrow(...)

        return null;
    }

    public RefundSuccessResponse refund (UUID userId, UUID orderId){
//        Todo
//        Orders order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));
//
//        if(!order.getUser().getId().equals(userId)){
//            throw new CustomException(ErrorCode.ACCESS_DENIED);
//        }


        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(()-> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

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
    public PaymentResponse getPayment(UUID userId, UUID orderId){

        //Todo : 사용자 권한 검증 추가

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentLogResponse> getPaymentLogs(UUID userId, UUID orderId){

        //Todo: 사용자 권한 검증 추가

        List<PaymentLogResponse> responseList = new ArrayList<>();

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(()-> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        List<PaymentLog> paymentLogs = paymentLogRepository.findAllByPayment(payment);

        for(PaymentLog paymentLog : paymentLogs){
            responseList.add(PaymentLogResponse.from(paymentLog));
        }

        return responseList;
    }
}
