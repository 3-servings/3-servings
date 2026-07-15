package com.sparta.server.threeserving.ai.repository;

import com.sparta.server.threeserving.ai.entity.AiGenerationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AiGenerationLogRepository  extends JpaRepository<AiGenerationLog, UUID> {
}
