package com.sparta.server.threeserving.global.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {

    SUCCESS(HttpStatus.OK, "SUCCESS", "요청이 성공했습니다."),
    CREATED(HttpStatus.CREATED, "SUCCESS", "생성이 완료되었습니다."),
    UPDATED(HttpStatus.OK, "SUCCESS", "수정이 완료되었습니다."),
    DELETED(HttpStatus.OK, "SUCCESS", "삭제가 완료되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
