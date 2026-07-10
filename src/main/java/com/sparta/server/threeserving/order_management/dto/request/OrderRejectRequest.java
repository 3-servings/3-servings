package com.sparta.server.threeserving.order_management.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Getter
@NoArgsConstructor
public class OrderRejectRequest {

    private UUID rejectReasonCodeId;
    private String memo;

}