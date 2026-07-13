package com.sparta.server.threeserving.payment.dto.response;

import com.sparta.server.threeserving.payment.entity.PaymentLog;
import com.sparta.server.threeserving.payment.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class PaymentLogResponse {

    private PaymentStatus status;

    private String message;

    private Instant createdAt;

    public static PaymentLogResponse from(PaymentLog paymentLog){
        return PaymentLogResponse.builder()
                .status(paymentLog.getStatus())
                .message(paymentLog.getMessage())
                .createdAt(paymentLog.getCreatedAt())
                .build();
    }
}
