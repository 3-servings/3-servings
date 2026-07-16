package com.sparta.server.threeserving.order_management.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class CreateRejectReasonCodeRequest {

    private String code;
    private String description;

}
