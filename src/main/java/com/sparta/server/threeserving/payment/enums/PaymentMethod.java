package com.sparta.server.threeserving.payment.enums;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import lombok.Getter;

@Getter
public enum PaymentMethod {
    CARD("카드"),
    CASH("현금"),
    KAKAO_PAY("카카오페이"),
    NAVER_PAY("네이버페이");

    private final String koreanName;

    // 생성자
    PaymentMethod(String koreanName) {
        this.koreanName = koreanName;
    }

    // 토스가 준 한글 문자열("카드")을 가지고 적절한 Enum(CARD)을 찾아주는 메서드
    public static PaymentMethod from(String tossMethod) {
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.getKoreanName().equals(tossMethod)) {
                return method;
            }
        }
        // 만약 매칭되는 결제 수단이 없다면 예외 발생 (프로젝트 내 예외 처리 방식을 적용하세요)
        throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
    }
}