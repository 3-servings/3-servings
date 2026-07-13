package com.sparta.server.threeserving.ai.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Table(name = "p_ai_generation_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AiGenerationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "store_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID storeId;

    // 로그의 독립성을 위해 연관관계 적용 x
    @Column(name = "image_id", nullable = true, updatable = false, columnDefinition = "uuid")
    private UUID imageId;

    // 점주가 입력한 가공되지 않은 순수 키워드/프롬프트
    @Column(name = "raw_prompt", nullable = false, updatable = false, columnDefinition = "TEXT")
    private String rawPrompt;

    @Column(name = "ai_request", nullable = false, updatable = false, columnDefinition = "TEXT")
    private String aiRequest;

    @Column(name = "ai_response", nullable = true, updatable = false, columnDefinition = "TEXT")
    private String aiResponse;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public AiGenerationLog(UUID storeId, UUID imageId, String rawPrompt, String aiRequest, String aiResponse) {
        this.storeId = storeId;
        this.imageId = imageId;
        this.rawPrompt = rawPrompt;
        this.aiRequest = aiRequest;
        this.aiResponse = aiResponse;
    }
}

