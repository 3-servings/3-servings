package com.sparta.server.threeserving.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.server.threeserving.ai.dto.request.AiMenuDescriptionRequest;
import com.sparta.server.threeserving.ai.dto.response.AiMenuDescriptionResponse;
import com.sparta.server.threeserving.ai.enums.AiGenerationStatus;
import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiMenuDescriptionService {

    private final ChatClient chatClient;
    private final AiGenerationLogService aiGenerationLogService;
    private final ObjectMapper objectMapper;

    @Value("classpath:/prompts/menu-description-system-prompt.st")
    private Resource systemPromptResource;

    @Value("classpath:/prompts/menu-description-user-prompt.st")
    private Resource userPromptResource;

    public AiMenuDescriptionResponse generateMenuDescription(AiMenuDescriptionRequest request) {
        String rawPromptJson = "";
        String aiRequestString = "";
        String rawAiResponse = "";
        AiGenerationStatus status = AiGenerationStatus.SUCCESS;
        AiMenuDescriptionResponse responseDto;

        try {
            rawPromptJson = objectMapper.writeValueAsString(request);

            PromptTemplate systemTemplate = new PromptTemplate(systemPromptResource);
            String systemMessage = systemTemplate.render();

            PromptTemplate userTemplate = new PromptTemplate(userPromptResource);
            Map<String, Object> variables = Map.of(
                    "name", request.getName(),
                    "price", request.getPrice() != null ? request.getPrice() : "가격 미정",
                    "ingredients", formatListToString(request.getIngredients()),
                    "flavorProfile", formatListToString(request.getFlavorProfile()),
                    "contextTags", formatListToString(request.getContextTags()),
                    "baseTone", request.getBaseTone().getDescription(),
                    "additionalRequest", request.getAdditionalRequest() != null ? request.getAdditionalRequest() : "없음"
            );
            String userMessage = userTemplate.render(variables);

            aiRequestString = "[SYSTEM]\n" + systemMessage + "\n\n[USER]\n" + userMessage;

            rawAiResponse = chatClient.prompt()
                    .system(systemMessage)
                    .user(userMessage)
                    .call()
                    .content();

            if (rawAiResponse == null || rawAiResponse.isBlank()) {
                throw new CustomException(ErrorCode.AI_EMPTY_RESPONSE);
            }

            BeanOutputConverter<AiMenuDescriptionResponse> converter = new BeanOutputConverter<>(AiMenuDescriptionResponse.class);
            responseDto = converter.convert(rawAiResponse);

            log.info("AI Description generated successfully - StoreId: {}, MenuId: {}", request.getStoreId(), request.getMenuId());

        } catch (CustomException e) {
            log.warn("AI validation failed - StoreId: {}, MenuId: {}, ErrorCode: {}", request.getStoreId(), request.getMenuId(), e.getErrorCode());
            status = AiGenerationStatus.FAIL_SYSTEM;
            throw e;

        } catch (JsonProcessingException e) {
            log.error("AI response parsing failed - StoreId: {}, MenuId: {}", request.getStoreId(), request.getMenuId(), e);
            status = AiGenerationStatus.FAIL_PARSING;
            throw new CustomException(ErrorCode.AI_RESPONSE_PARSE_ERROR);

        } catch (Exception e) {
            log.error("Gemini API call error or timeout - StoreId: {}, MenuId: {}", request.getStoreId(), request.getMenuId(), e);
            status = AiGenerationStatus.FAIL_SYSTEM;
            throw new CustomException(ErrorCode.AI_GENERATION_FAILED);

        } finally {
            aiGenerationLogService.saveLog(
                    request.getStoreId(),
                    request.getMenuId(),
                    rawPromptJson,
                    aiRequestString,
                    rawAiResponse,
                    status
            );
        }

        return responseDto;
    }

    private String formatListToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "없음";
        }
        return String.join(", ", list);
    }
}
