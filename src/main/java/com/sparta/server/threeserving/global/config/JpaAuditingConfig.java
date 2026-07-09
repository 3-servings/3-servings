package com.sparta.server.threeserving.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Instant;
import java.util.Optional;

@EnableJpaAuditing(dateTimeProviderRef = "utcDateTimeProvider")
@Configuration
public class JpaAuditingConfig {

    @Bean
    public DateTimeProvider utcDateTimeProvider(){
        return () -> Optional.of(Instant.now());
    }

    // test 용 임시 코드
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            // 현재 SecurityContext에서 사용자를 꺼내거나,
            // 당장 테스트가 필요하다면 임시로 '1L'을 반환하게 합니다.
            return Optional.of(1L);
        };
    }
}
