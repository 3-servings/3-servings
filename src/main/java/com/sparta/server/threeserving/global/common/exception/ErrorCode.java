package com.sparta.server.threeserving.global.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    UNSUPPORTED_METHOD(HttpStatus.BAD_REQUEST, "C002", "지원하지 않는 방식입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "C003", "인증 토큰이 유효하지 않습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C004", "접근 권한이 없습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C005", "요청한 리소스를 찾을 수 없습니다."),
    BUSINESS_RULE_VIOLATION(HttpStatus.CONFLICT, "C006", "비즈니스 규칙에 위배되는 요청입니다."),

    // Store
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "가게를 찾을 수 없습니다."),

    // Menu
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "메뉴를 찾을 수 없습니다."),
    DELETED_MENU_STATUS_CHANGE(HttpStatus.CONFLICT, "M002", "삭제된 메뉴의 상태는 변경할 수 없습니다."),

    // Order/Cart
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "장바구니를 찾을 수 없습니다."),
    NOT_CART_OWNER(HttpStatus.FORBIDDEN, "O002", "본인의 장바구니가 아닙니다."),

    // System
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S999", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
