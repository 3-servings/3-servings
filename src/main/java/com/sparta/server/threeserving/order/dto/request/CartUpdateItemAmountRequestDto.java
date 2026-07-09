package com.sparta.server.threeserving.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartUpdateItemAmountRequestDto(
        @NotNull @Min(1) Integer quantity
)
{

}
