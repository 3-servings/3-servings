package com.sparta.server.threeserving.menu.controller;

import com.sparta.server.threeserving.auth.UserDetailsImpl;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.menu.dto.request.MenuCreateRequest;
import com.sparta.server.threeserving.menu.dto.response.MenuResponse;
import com.sparta.server.threeserving.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @PostMapping("/stores/{storeId}/menus")
    public ResponseEntity<ApiResponse<MenuResponse>> createMenu(
            @PathVariable UUID storeId,
            @Valid @RequestBody MenuCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        MenuResponse response = menuService.createMenu(
                storeId,
                request,
                userDetails.getUser().getId(),
                userDetails.getUser().getRole()
        );

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CREATED, response));
    }

}
