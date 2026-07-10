package com.sparta.server.threeserving.order_management.dto.request;

import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderStatusUpdateRequest {

    @NotNull
    private OrderStatusEnum status;

}
