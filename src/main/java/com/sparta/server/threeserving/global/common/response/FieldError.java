package com.sparta.server.threeserving.global.common.response;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
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


    public static FieldError from(org.springframework.validation.FieldError fieldError){
        return FieldError.builder()
                .field(fieldError.getField())
                .value(fieldError.getRejectedValue())
                .reason(fieldError.getDefaultMessage())
                .build();
    }
}
