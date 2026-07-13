package com.sparta.server.threeserving.order_management.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderAcceptRequest {

    @Min(value = 1, message = "예상 조리 시간은 1분 이상이어야 합니다.")
    private Integer estimatedCookTime;
}

