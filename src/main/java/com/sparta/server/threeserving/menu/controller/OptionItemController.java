package com.sparta.server.threeserving.menu.controller;

import com.sparta.server.threeserving.auth.UserDetailsImpl;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.menu.dto.request.OptionItemStatusUpdateRequest;
import com.sparta.server.threeserving.menu.service.OptionItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OptionItemController {

    private final OptionItemService optionItemService;

    @PatchMapping("/option-items/status")
    public ResponseEntity<ApiResponse<Void>> updateOptionItemsStatus(
            @Valid @RequestBody OptionItemStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        optionItemService.updateOptionItemsStatus(
                request,
                userDetails.getUser().getId(),
                userDetails.getUser().getRole()
        );

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.UPDATED));
    }

}
