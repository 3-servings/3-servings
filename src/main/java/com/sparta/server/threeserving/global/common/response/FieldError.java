package com.sparta.server.threeserving.global.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FieldError {
    private String field;
    private Object value;
    private String reason;
}
