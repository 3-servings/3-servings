package com.sparta.server.threeserving.order.dto.response;

import java.util.UUID;

public record CartUpdateItemAmountResponseDto(
        UUID id,
        Integer quantity
)
{

}
