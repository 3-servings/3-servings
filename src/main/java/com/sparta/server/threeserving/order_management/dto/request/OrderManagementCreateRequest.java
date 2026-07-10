package com.sparta.server.threeserving.order_management.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class OrderManagementCreateRequest {

    private UUID orderId;

}