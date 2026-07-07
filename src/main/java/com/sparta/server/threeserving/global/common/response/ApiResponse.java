package com.sparta.server.threeserving.global.common.response;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ApiResponse<T> {

    private String code;
    private String message;
    private T data;
    private List<FieldError> errors;


    private ApiResponse(String code, String message, T data, List<FieldError> errors) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    // =========================
    // SUCCESS
    // =========================
    //데이터 있는 성공 응답
    public static <T> ApiResponse<T> success(SuccessCode successCode, T data) {
        return new ApiResponse<>(
                successCode.getCode(),
                successCode.getMessage(),
                data,
                null
        );
    }

    //데이터 없는 성공 응답
    public static ApiResponse<Void> success(SuccessCode successCode) {
        return new ApiResponse<>(
                successCode.getCode(),
                successCode.getMessage(),
                null,
                null
        );
    }

    // =========================
    // FAIL
    // =========================
    //  실패 (validation)
    public static ApiResponse<Void> fail(ErrorCode errorCode, List<FieldError> errors) {
        return new ApiResponse<>(
                errorCode.getCode(),
                errorCode.getMessage(),
                null,
                errors
        );
    }

    //  실패 (business)
    public static ApiResponse<Void> fail(ErrorCode errorCode) {
        return new ApiResponse<>(
                errorCode.getCode(),
                errorCode.getMessage(),
                null,
                null
        );
    }
}
