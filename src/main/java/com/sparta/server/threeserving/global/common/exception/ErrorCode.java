package com.sparta.server.threeserving.global.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Image (Presigned)
    IMAGE_PRESIGN_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMG001", "이미지 업로드 URL 발급에 실패했습니다."),
    INVALID_IMAGE_TYPE(HttpStatus.BAD_REQUEST,             "IMG002", "이미지 파일만 업로드할 수 있습니다."),
    IMAGE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST,           "IMG003", "이미지는 최대 5장까지 첨부할 수 있습니다."),
    INVALID_IMAGE_PATH(HttpStatus.BAD_REQUEST,             "IMG004", "허용되지 않은 이미지 경로입니다."),
    IMAGE_NOT_UPLOADED(HttpStatus.BAD_REQUEST,             "IMG005", "업로드되지 않은 이미지입니다. 다시 시도해주세요."),
    // User / Auth
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "U002", "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "U003", "비밀번호가 일치하지 않습니다."),
    USERNAME_DUPLICATED(HttpStatus.CONFLICT, "U004", "이미 사용 중인 아이디입니다."),
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "U005", "이미 사용 중인 닉네임입니다."),
    DUPLICATED_RESOURCE(HttpStatus.CONFLICT, "U006", "이미 사용 중인 정보입니다."),
    ALREADY_WITHDRAWN(HttpStatus.CONFLICT,     "U007", "이미 탈퇴한 회원입니다."),
    SAME_AS_OLD_PASSWORD(HttpStatus.BAD_REQUEST,"U008", "기존 비밀번호와 동일합니다."),
    EMAIL_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "U009", "인증코드가 일치하지 않습니다."),
    EMAIL_CODE_EXPIRED(HttpStatus.BAD_REQUEST,  "U010", "인증코드가 만료되었습니다. 다시 요청해주세요."),
    EMAIL_SEND_FAILED(HttpStatus.BAD_GATEWAY,   "U011", "이메일 발송에 실패했습니다."),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "만료된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A003", "재발급이 불가능합니다. 다시 로그인해주세요."),
    KAKAO_AUTH_FAILED(HttpStatus.BAD_GATEWAY, "A004", "카카오 인증에 실패했습니다."),
    INVALID_ID_PASSWORD(HttpStatus.UNAUTHORIZED, "A005", "아이디 및 비번이 틀렸습니다."),

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
    MENU_CATEGORY_HAS_MENUS(HttpStatus.CONFLICT, "M008", "해당 카테고리에 등록된 메뉴가 존재합니다. 메뉴를 비우거나 이동한 후 다시 시도해주세요."),

    OPTION_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "M004", "옵션 그룹을 찾을 수 없습니다."),
    OPTION_GROUP_NAME_DUPLICATED(HttpStatus.CONFLICT, "M005", "이미 존재하는 옵션 그룹 이름입니다."),

    OPTION_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "M006", "옵션 아이템을 찾을 수 없습니다."),
    INVALID_OPTION_SELECTION(HttpStatus.BAD_REQUEST, "O004", "최소/최대 선택 개수가 올바르지 않습니다."),
    OPTION_MIN_SELECT_VIOLATION(HttpStatus.BAD_REQUEST, "M007", "필수 옵션의 판매 가능한 항목이 최소 선택 개수보다 적어질 수 없습니다."),

    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "M000", "메뉴를 찾을 수 없습니다."),
    MENU_NAME_DUPLICATED(HttpStatus.CONFLICT, "M002", "이미 존재하는 메뉴 이름입니다."),
    MENU_STORE_MISMATCH(HttpStatus.BAD_REQUEST, "M002", "해당 메뉴는 요청한 가게에 속해 있지 않습니다."),
    MENU_MENU_CATEGORY_MISMATCH(HttpStatus.BAD_REQUEST, "M002", "해당 메뉴는 요청한 카테고리에 속해 있지 않습니다."),
    OPTION_GROUP_STORE_MISMATCH(HttpStatus.BAD_REQUEST, "M002", "해당 옵션 그룹은 요청한 가게에 속해 있지 않습니다."),

    // Image
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "I001", "파일 확장자가 존재하지 않습니다."),
    UNSUPPORTED_FILE_FORMAT(HttpStatus.BAD_REQUEST, "I002", "지원하지 않는 이미지 형식입니다. (jpg, jpeg, png, webp만 허용)"),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "I003", "이미지를 찾을 수 없습니다."),
    IMAGE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "I004", "이미지 처리(URL 발급) 중 오류가 발생했습니다."),

    // AI
    AI_RESPONSE_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI_001", "AI 응답을 처리하는 중 오류가 발생했습니다."),
    AI_GENERATION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "AI_002", "AI 메뉴 설명 생성에 실패했습니다. 잠시 후 다시 시도해주세요."),
    AI_EMPTY_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "AI_003", "AI 모델이 빈 응답을 반환했습니다."),

    // Order/Cart
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "장바구니를 찾을 수 없습니다."),
    NOT_CART_OWNER(HttpStatus.FORBIDDEN, "O002", "본인의 장바구니가 아닙니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O003", "주문정보를 찾을 수 없습니다."),
    NOT_ORDER_OWNER(HttpStatus.FORBIDDEN, "O004", "본인의 주문이 아닙니다."),
    NOT_STORE_OWNER_OF_ORDER(HttpStatus.FORBIDDEN, "O005", "본인 가게의 주문이 아닙니다."),
    ADD_UNAVAILABLE_MENU_TO_CART(HttpStatus.FORBIDDEN, "O006", "현재 주문할 수 없는 메뉴입니다."),
    ADD_MENU_OF_OTHER_STORE_TO_CART(HttpStatus.FORBIDDEN, "O007", "다른 가게의 주문입니다."),
    CART_OPTION_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "O008", "옵션을 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "O009", "장바구니에 존재하지 않는 항목이거나 다른 카트에 속합니다."),
    ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "O010", "주문 정보에 존재하지 않는 항목이거나 다른 주문 정보에 속합니다."),
    EXPIRED_CANCEL_TIME(HttpStatus.BAD_REQUEST, "O011", "주문 취소 가능 시간이 지났습니다."),
    ORDER_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "O014", "이미 주문이 처리되었습니다."),
    ORDER_ITEMS_IS_EMPTY(HttpStatus.BAD_REQUEST, "O015", "주문 항목이 비어있습니다."),
    CART_OPTION_NOT_BELONG_TO_MENU(HttpStatus.CONFLICT, "O0016", "해당 메뉴에 속하지 않는 옵션입니다."),
    CART_OPTION_GROUP_SELECTION_INVALID(HttpStatus.CONFLICT, "O0017", "옵션 그룹의 선택 개수 제한을 위반했습니다."),

    // System
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S999", "서버 내부 오류가 발생했습니다."),

    // Review
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "리뷰를 찾을 수 없습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "R002", "이미 해당 주문에 리뷰를 작성했습니다."),
    REVIEW_NOT_OWNER(HttpStatus.FORBIDDEN, "R003", "본인이 작성한 리뷰만 수정/삭제할 수 있습니다."),
    ORDER_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "R004", "배달이 완료된 주문에만 리뷰를 작성할 수 있습니다."),
    NOT_ORDER_OWNER_OF_REVIEW(HttpStatus.FORBIDDEN, "R005", "본인의 주문에만 리뷰를 작성할 수 있습니다."),

    // Review Comment (사장 답글)
    REVIEW_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "RC001", "답글을 찾을 수 없습니다."),
    REVIEW_COMMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "RC002", "이미 답글을 작성했습니다."),
    NOT_STORE_OWNER(HttpStatus.FORBIDDEN, "RC003", "본인 가게의 리뷰에만 답글을 작성/수정할 수 있습니다."),

    // OrderManagement
    ORDER_MANAGEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "OM001", "주문 상태 정보를 찾을 수 없습니다."),
    ORDER_STATUS_INVALID(HttpStatus.BAD_REQUEST, "OM002", "변경할 수 없는 주문 상태입니다."),
    ORDER_STATUS_ALREADY_CHANGED(HttpStatus.CONFLICT, "OM003", "이미 처리된 주문입니다."),
    ORDER_STATUS_TRANSITION_INVALID(HttpStatus.BAD_REQUEST, "OM004", "허용되지 않는 주문 상태 변경입니다."),
    ORDER_MANAGEMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "OM005", "해당 주문에 접근할 권한이 없습니다."),
    ORDER_MANAGEMENT_STORE_MISMATCH(HttpStatus.FORBIDDEN, "OM006", "본인 매장의 주문만 조회 및 처리할 수 있습니다."),
    ESTIMATED_COOK_TIME_INVALID(HttpStatus.BAD_REQUEST, "OM007", "예상 조리 시간은 0분보다 커야 합니다."),
    REJECT_MEMO_REQUIRED(HttpStatus.BAD_REQUEST, "OM008", "주문 거절 사유를 입력해주세요."),
    FORBIDDEN_STORE_ACCESS(HttpStatus.FORBIDDEN,"O019","해당 가게에 접근 권한이 없습니다."),

    //Payment
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "결제 정보를 찾을 수 없습니다."),
    PAYMENT_ALREADY_REFUNDED(HttpStatus.CONFLICT, "P002", "이미 환불된 결제입니다."),
    REFUND_EXPIRED(HttpStatus.BAD_REQUEST, "P003", "환불 가능 시간이 만료되었습니다."),
    PAYMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "P004", "이미 결제가 완료된 주문입니다."),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "P005", "주문 금액과 결제 금액이 일치하지 않습니다."),
    PAYMENT_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "P006", "결제 서버 응답 시간이 초과되었습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
