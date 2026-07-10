package com.sparta.server.threeserving.payment.dto.response;

import com.sparta.server.threeserving.payment.entity.Payment;
import com.sparta.server.threeserving.payment.enums.PaymentMethod;
import com.sparta.server.threeserving.payment.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class PaymentResponse {

    private UUID orderId;

    private PaymentMethod paymentMethod;

    private Long amount;

    private String transactionId;

    private PaymentStatus status;

    private Instant requestedAt;

    private Instant approvedAt;

    private Instant refundAt;

    public static PaymentResponse from(Payment payment){
        return PaymentResponse.builder()
                .orderId(payment.getOrder().getId())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .requestedAt(payment.getRequestedAt())
                .approvedAt(payment.getApprovedAt())
                .refundAt(payment.getRefundAt())
                .build();
    }
}
