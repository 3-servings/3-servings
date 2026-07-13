package com.sparta.server.threeserving.global.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // User / Auth
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "U002", "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "U003", "비밀번호가 일치하지 않습니다."),
    USERNAME_DUPLICATED(HttpStatus.CONFLICT, "U004", "이미 사용 중인 아이디입니다."),
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "U005", "이미 사용 중인 닉네임입니다."),
    DUPLICATED_RESOURCE(HttpStatus.CONFLICT, "U006", "이미 사용 중인 정보입니다."),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "만료된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A003", "재발급이 불가능합니다. 다시 로그인해주세요."),
    KAKAO_AUTH_FAILED(HttpStatus.BAD_GATEWAY, "A004", "카카오 인증에 실패했습니다."),
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    UNSUPPORTED_METHOD(HttpStatus.BAD_REQUEST, "C002", "지원하지 않는 방식입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "C003", "인증 토큰이 유효하지 않습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C004", "접근 권한이 없습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C005", "요청한 리소스를 찾을 수 없습니다."),
    BUSINESS_RULE_VIOLATION(HttpStatus.CONFLICT, "C006", "비즈니스 규칙에 위배되는 요청입니다."),

    // Store
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "가게를 찾을 수 없습니다."),
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND,"S002" , "지역을 찾을 수 없습니다."),
    REGION_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "S003", "이미 삭제된 지역입니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "S004", "카테고리를 찾을 수 없습니다."),
    // Menu
    MENU_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "메뉴 카테고리를 찾을 수 없습니다."),
    MENU_CATEGORY_NAME_DUPLICATED(HttpStatus.CONFLICT, "M002", "이미 존재하는 메뉴 카테고리 이름입니다."),
    MENU_CATEGORY_ALREADY_DELETED(HttpStatus.CONFLICT, "M003", "이미 삭제된 메뉴 카테고리입니다."),
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "M000", "메뉴를 찾을 수 없습니다."),
    DELETED_MENU_STATUS_CHANGE(HttpStatus.CONFLICT, "M000", "삭제된 메뉴의 상태는 변경할 수 없습니다."),

    // Order/Cart
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "장바구니를 찾을 수 없습니다."),
    NOT_CART_OWNER(HttpStatus.FORBIDDEN, "O002", "본인의 장바구니가 아닙니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "O009", "장바구니에 존재하지 않는 항목이거나 다른 카트 소속입니다."),

    // System
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S999", "서버 내부 오류가 발생했습니다."),

    // OrderManagement
    ORDER_MANAGEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "OM001", "주문 상태 정보를 찾을 수 없습니다."),
    ORDER_STATUS_INVALID(HttpStatus.BAD_REQUEST, "OM002", "변경할 수 없는 주문 상태입니다."),
    ORDER_STATUS_ALREADY_CHANGED(HttpStatus.CONFLICT, "OM003", "이미 처리된 주문입니다."),
    ORDER_STATUS_TRANSITION_INVALID(HttpStatus.BAD_REQUEST, "OM004", "허용되지 않는 주문 상태 변경입니다."),
    ORDER_MANAGEMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "OM005", "해당 주문에 접근할 권한이 없습니다."),
    ORDER_MANAGEMENT_STORE_MISMATCH(HttpStatus.FORBIDDEN, "OM006", "본인 매장의 주문만 조회 및 처리할 수 있습니다."),
    ESTIMATED_COOK_TIME_INVALID(HttpStatus.BAD_REQUEST, "OM007", "예상 조리 시간은 0분보다 커야 합니다."),
    REJECT_MEMO_REQUIRED(HttpStatus.BAD_REQUEST, "OM008", "주문 거절 사유를 입력해주세요."),

    //Payment
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "결제 정보를 찾을 수 없습니다."),
    PAYMENT_ALREADY_REFUNDED(HttpStatus.CONFLICT, "P002", "이미 환불된 결제입니다."),
    REFUND_EXPIRED(HttpStatus.BAD_REQUEST, "P003", "환불 가능 시간이 만료되었습니다."),
    PAYMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "P004", "이미 결제가 완료된 주문입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
