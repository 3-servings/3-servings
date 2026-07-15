package com.sparta.server.threeserving.global.filter;

import com.sparta.server.threeserving.global.logging.MdcUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {


    private static final String TRACE_HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain filterChain) throws ServletException, IOException
    {
        long startTime = System.currentTimeMillis();

        String traceId = req.getHeader(TRACE_HEADER);
        if (!StringUtils.hasText(traceId)){
            traceId = UUID.randomUUID().toString().substring(0, 8);
        }
        MdcUtil.putTraceId(traceId);
        res.setHeader(TRACE_HEADER, traceId);

        try {
            // 2) 안쪽(Controller)으로 요청 전달
            filterChain.doFilter(req, res);
        } finally {
            // 3) 요청 종료 → 소요시간 계산 후 한 줄 로그
            long took = System.currentTimeMillis() - startTime;
            log.info("{} {} | status={} | {}ms | ip={}",
                    req.getMethod(),           // GET/POST...
                    getFullPath(req),          // 호출된 URL(+쿼리)
                    res.getStatus(),          // 응답 상태코드
                    took,                          // 응답까지 걸린 시간(ms)
                    req.getRemoteAddr());      // 요청 보낸 IP
            // 4) 반드시 정리 (스레드 재사용 대비)
            MDC.clear();
        }
    }

    private String getFullPath(HttpServletRequest req){
        String uri = req.getRequestURI();
        String query = req.getQueryString();
        return query == null ? uri : uri + "?" + query;
    }

    //로그 남길 필요없는 경로 제외
    @Override
    protected boolean shouldNotFilter(HttpServletRequest req){
        String path = req.getRequestURI();
        return path.startsWith("/actuator")
                || path.startsWith("/favicon")
                || path.startsWith("/error");
    }


}
/**
 * 실행 흐름
 * 클라이언트
 *     │
 *     ▼
 * RequestLoggingFilter   ← 여기 먼저 실행
 *     │
 *     ▼
 * DispatcherServlet
 *     │
 *     ▼
 * Controller(@GetMapping)
 *     │
 *     ▼
 * Response
 *     │
 *     ▼
 * RequestLoggingFilter finally ← 여기서 로그 출력
 */
