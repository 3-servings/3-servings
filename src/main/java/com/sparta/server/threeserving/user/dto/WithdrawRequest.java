package com.sparta.server.threeserving.user.dto;

import jakarta.validation.constraints.NotBlank;

//회원탈퇴 시 본인 확인용 비밀번호 재입력
public record WithdrawRequest(
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {
}
