package com.sparta.server.threeserving.menu.controller;

import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.menu.dto.request.OptionGroupCreateRequest;
import com.sparta.server.threeserving.menu.dto.response.OptionGroupResponse;
import com.sparta.server.threeserving.menu.service.OptionGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OptionGroupController {

    private final OptionGroupService optionGroupService;

    @PostMapping("/stores/{storeId}/option-groups")
    public ResponseEntity<ApiResponse<OptionGroupResponse>> createOptionGroup(
            @PathVariable UUID storeId,
            @Valid @RequestBody OptionGroupCreateRequest request
    ) {
        OptionGroupResponse response = OptionGroupResponse.from(
                optionGroupService.createOptionGroup(storeId, request)
        );

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CREATED, response));
    }

    @GetMapping("/stores/{storeId}/option-groups")
    public ResponseEntity<ApiResponse<Page<OptionGroupResponse>>> getOptionGroups(
            @PathVariable UUID storeId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<OptionGroupResponse> responses = optionGroupService.getOptionGroups(storeId, keyword, pageable);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SUCCESS, responses));
    }

}
