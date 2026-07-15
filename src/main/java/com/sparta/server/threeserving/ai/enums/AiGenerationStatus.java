package com.sparta.server.threeserving.ai.enums;

public enum AiGenerationStatus {
    SUCCESS,        // 성공적으로 텍스트를 생성하고 파싱까지 완료한 상태
    FAIL_TIMEOUT,   // AI API 호출 중 시간 초과 발생
    FAIL_PARSING,   // AI가 응답을 주었으나 지정된 JSON 규격을 어겨 파싱에 실패한 상태
    FAIL_SYSTEM     // 기타 시스템 또는 네트워크 오류
}