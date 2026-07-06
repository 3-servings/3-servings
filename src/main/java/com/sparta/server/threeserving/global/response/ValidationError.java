package com.sparta.server.threeserving.global.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ValidationError {

    private String field;
    private Object value;
    private String reason;

    public static ValidationError from(org.springframework.validation.FieldError fieldError) {
        return ValidationError.builder()
                .field(fieldError.getField())
                .value(fieldError.getRejectedValue())
                .reason(fieldError.getDefaultMessage())
                .build();
    }
}