package com.sparta.server.threeserving.payment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TossConfirmRequest {

    private String paymentKey;

    private String orderId;

    private Long amount;
}
