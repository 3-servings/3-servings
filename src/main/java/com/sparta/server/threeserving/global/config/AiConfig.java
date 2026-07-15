package com.sparta.server.threeserving.global.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;
import java.util.Arrays;

@Configuration
public class AiConfig {

    @Bean
    public RestClientCustomizer aiRestClientCustomizer() {
        return restClientBuilder -> {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(Duration.ofSeconds(3));   // 서버 연결 자체는 3초 안에 안 되면 실패
            factory.setReadTimeout(Duration.ofSeconds(10));     // 연결 후 응답이 10초 안에 안 오면 타임아웃

            restClientBuilder.requestFactory(factory);
        };
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, Environment env) {
        // 현재 실행 환경 Profile 확인
        boolean isLocal = Arrays.asList(env.getActiveProfiles()).contains("local");

        // 로컬 환경일 때만 로거 어드바이저 추가 (디버깅용)
        if (isLocal) {
            builder.defaultAdvisors(new SimpleLoggerAdvisor());
        }

        return builder.build();
    }
}
