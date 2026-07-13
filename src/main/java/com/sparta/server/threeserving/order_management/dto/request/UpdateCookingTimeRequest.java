package com.sparta.server.threeserving.order_management.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateCookingTimeRequest {

    @NotNull
    @Positive
    private Integer estimatedCookTime;

}
