package com.sparta.server.threeserving.review.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
//사장 답글 작성/수정 공용
public record ReviewCommentRequest(
        @NotNull(message = "답글 내용을 입력해주세요")
        @Size(max = 1000, message = "답글은 1000자를 넘을 수 없습니다.")
        String content
) {
}
