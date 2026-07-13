package com.sparta.server.threeserving.payment.dto.response;

import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class TossConfirmResponse {

    private String paymentKey;

    private String orderId;

    private String method;

    private String status;

    private Long totalAmount;

    private OffsetDateTime approvedAt;

}
