package com.sparta.server.threeserving.payment.dto.response;

import com.sparta.server.threeserving.payment.entity.Payment;
import com.sparta.server.threeserving.payment.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class RefundSuccessResponse {

    private String message;

    private UUID orderId;

    private PaymentStatus status;

    private Instant refundAt;

    public static RefundSuccessResponse from(Payment payment){
        return RefundSuccessResponse.builder()
                .message("Refund success")
                .orderId(payment.getOrder().getId())
                .status(payment.getStatus())
                .refundAt(payment.getRefundAt())
                .build();
    }
}
