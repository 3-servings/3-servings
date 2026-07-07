package com.sparta.server.threeserving.global.exception;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}