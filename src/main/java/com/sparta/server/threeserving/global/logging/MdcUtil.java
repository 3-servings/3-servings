package com.sparta.server.threeserving.global.logging;

import org.slf4j.MDC;

public final class MdcUtil {

    public static final String TRACE_ID = "traceId";
    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";

    private MdcUtil() {
    }

    public static void putTraceId(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    // 정리는 RequestLoggingFilter 의 MDC.clear() 가 전담한다. 여기서 remove 하면
    // 요청 요약 로그(필터 finally)에서 값이 이미 사라져 버린다.
    public static void putUser(Long userId, String username) {
        MDC.put(USER_ID, String.valueOf(userId));
        MDC.put(USERNAME, username);
    }
}
