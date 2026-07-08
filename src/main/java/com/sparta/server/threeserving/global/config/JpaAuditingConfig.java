package com.sparta.server.threeserving.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
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
}
