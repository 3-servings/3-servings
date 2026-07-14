package com.sparta.server.threeserving.payment.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.order.entity.Orders;
import com.sparta.server.threeserving.order.repository.OrderRepository;
import com.sparta.server.threeserving.order_management.dto.request.OrderManagementCreateRequest;
import com.sparta.server.threeserving.order_management.service.OrderManagementService;
import com.sparta.server.threeserving.payment.dto.request.PaymentRequest;
import com.sparta.server.threeserving.payment.dto.request.TossConfirmRequest;
import com.sparta.server.threeserving.payment.dto.response.PaymentLogResponse;
import com.sparta.server.threeserving.payment.dto.response.PaymentResponse;
import com.sparta.server.threeserving.payment.dto.response.RefundSuccessResponse;
import com.sparta.server.threeserving.payment.dto.response.TossConfirmResponse;
import com.sparta.server.threeserving.payment.entity.Payment;
import com.sparta.server.threeserving.payment.entity.PaymentLog;
import com.sparta.server.threeserving.payment.enums.PaymentStatus;
import com.sparta.server.threeserving.payment.repository.PaymentLogRepository;
import com.sparta.server.threeserving.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentLogRepository paymentLogRepository;
    private final OrderRepository orderRepository;
    private final OrderManagementService orderManagementService;
    private final RestClient restClient;

    @Value("${toss.secret-key}")
    private String secretKey;

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

    private TossConfirmResponse confirmWithToss(TossConfirmRequest request) {
        String encodedKey = Base64.getEncoder()
                .encodeToString((secretKey +":").getBytes(StandardCharsets.UTF_8));

        Map<String, Object> body = Map.of(
                "paymentKey", request.getPaymentKey(),
                "orderId", request.getOrderId(),
                "amount", request.getAmount()
        );

        try{
            return restClient.post()
                    .uri("https://api.tosspayments.com/v1/payments/confirm")
                    .header(HttpHeaders.AUTHORIZATION, "Basic "+encodedKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(TossConfirmResponse.class);
        } catch (ResourceAccessException e){

            throw new CustomException(ErrorCode.PAYMENT_TIMEOUT);
        }
    }

    private void cancelWithToss(String paymentKey){
        String encodedKey = Base64.getEncoder()
                .encodeToString((secretKey+":").getBytes(StandardCharsets.UTF_8));

        try{
            restClient.post()
                    .uri("https://api.tosspayments.com/v1/payments/{paymentKey}/cancel",
                            paymentKey)
                    .header(HttpHeaders.AUTHORIZATION,
                            "Basic "+encodedKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "cancelReason", "사용자 요청"
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (ResourceAccessException e){
            throw new CustomException(ErrorCode.PAYMENT_TIMEOUT);
        }
    }

    private void validatePaymentAmount(Orders order, Long amount) {
        if(!Objects.equals(order.getTotalPrice().longValue(), amount)){
            throw new CustomException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }
    }

    private void validateRefundable(Payment payment) {
        if(payment.getStatus() == PaymentStatus.REFUNDED){
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_REFUNDED);
        }

        if(Duration.between(payment.getApprovedAt(), Instant.now()).toMinutes() >= 5){
            throw new CustomException(ErrorCode.REFUND_EXPIRED);
        }
    }

    private PaymentResponse saveConfirmPayment(
            Orders order,
            TossConfirmResponse response
    ) {
        Payment payment = Payment.createFromToss(order, response);
        Payment savedPayment = paymentRepository.save(payment);

        paymentLogRepository.save(
                PaymentLog.create(
                        savedPayment,
                        PaymentStatus.SUCCESS,
                        "결제 완료"
                )
        );

        return PaymentResponse.from(savedPayment);
    }

    public PaymentResponse confirmPayment(Long userId, UUID orderId, TossConfirmRequest request){
        Orders orders = validateOrder(userId, orderId);

        validatePaymentAmount(orders, request.getAmount());

        if(paymentRepository.findByOrderId(orderId).isPresent()){
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        //외부 API 호출
        TossConfirmResponse tossResponse = confirmWithToss(request);

        try{
            return saveConfirmPayment(orders, tossResponse);

        } catch (Exception e){
            //DB 실패 시 토스 취소(보상 트랜잭션)
            cancelWithToss(tossResponse.getPaymentKey());

            throw e;
        }

    }

    @Transactional
    public RefundSuccessResponse refundWithToss(Long userId, UUID orderId){

        validateOrder(userId, orderId);
        Payment payment = findPayment(orderId);

        validateRefundable(payment);

        paymentLogRepository.save(
                PaymentLog.create(
                        payment,
                        PaymentStatus.REFUND_REQUESTED,
                        "환불 요청"
                )
        );

        cancelWithToss(payment.getTransactionId());

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

    @Transactional
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

//        orderManagementService.create(
//                new OrderManagementCreateRequest(savedPayment.getOrder().getId())
//        );

        return PaymentResponse.from(savedPayment);
    }

    @Transactional
    public RefundSuccessResponse refund (Long userId, UUID orderId){

        validateOrder(userId, orderId);
        Payment payment = findPayment(orderId);

        validateRefundable(payment);

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
