package com.sparta.server.threeserving.ai.controller;

import com.sparta.server.threeserving.ai.dto.request.AiMenuDescriptionRequest;
import com.sparta.server.threeserving.ai.dto.response.AiMenuDescriptionResponse;
import com.sparta.server.threeserving.ai.service.AiMenuDescriptionService;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AiMenuDescriptionController {

    private final AiMenuDescriptionService aiMenuDescriptionService;

    @PostMapping("/ai/description")
    public ResponseEntity<ApiResponse<AiMenuDescriptionResponse>> generateAiDescription(
            @Valid @RequestBody AiMenuDescriptionRequest request
    ) {
        AiMenuDescriptionResponse response = aiMenuDescriptionService.generateMenuDescription(request);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SUCCESS, response));
    }
}
