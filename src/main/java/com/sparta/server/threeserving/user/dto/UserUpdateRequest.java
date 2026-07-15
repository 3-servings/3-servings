package com.sparta.server.threeserving.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// 본인 프로필 수정: 닉네임/전화번호만 변경 가능 (권한/상턔/이메일 등은 변경 불가)
public record UserUpdateRequest(
        @Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다.")
        String nickname,

        @Pattern(regexp = "^010\\d{8}$", message = "휴대폰 번호 형식이 올바르지 안습니다.")
        String phone
) {
}
