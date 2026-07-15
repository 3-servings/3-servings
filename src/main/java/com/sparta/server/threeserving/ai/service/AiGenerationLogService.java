package com.sparta.server.threeserving.ai.service;

import com.sparta.server.threeserving.ai.entity.AiGenerationLog;
import com.sparta.server.threeserving.ai.enums.AiGenerationStatus;
import com.sparta.server.threeserving.ai.repository.AiGenerationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiGenerationLogService {

    private final AiGenerationLogRepository aiGenerationLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(UUID storeId, UUID menuId, String rawPrompt, String aiRequest, String aiResponse, AiGenerationStatus status) {
        AiGenerationLog aiLog = AiGenerationLog.builder()
                .storeId(storeId)
                .menuId(menuId)
                .rawPrompt(rawPrompt)
                .aiRequest(aiRequest)
                .aiResponse(aiResponse)
                .status(status)
                .build();
        aiGenerationLogRepository.save(aiLog);
    }
}
