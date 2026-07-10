package com.sparta.server.threeserving.payment.dto.request;

import com.sparta.server.threeserving.payment.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull
    private PaymentMethod paymentMethod;
}
