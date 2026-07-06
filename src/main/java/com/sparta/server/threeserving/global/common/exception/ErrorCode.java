package com.sparta.server.threeserving.global.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
