package com.sparta.server.threeserving.menu.controller;

import com.sparta.server.threeserving.auth.UserDetailsImpl;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.menu.dto.request.MenuCategoryCreateRequest;
import com.sparta.server.threeserving.menu.dto.request.MenuCategoryDisplayOrderUpdateRequest;
import com.sparta.server.threeserving.menu.dto.request.MenuCategoryUpdateRequest;
import com.sparta.server.threeserving.menu.dto.response.MenuCategoryResponse;
import com.sparta.server.threeserving.menu.service.MenuCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MenuCategoryController {

    private final MenuCategoryService menuCategoryService;

    @PostMapping("/stores/{storeId}/menu-categories")
    public ResponseEntity<ApiResponse<MenuCategoryResponse>> createMenuCategory(
            @PathVariable UUID storeId,
            @Valid @RequestBody MenuCategoryCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        MenuCategoryResponse response = menuCategoryService.createMenuCategory(
                storeId,
                request,
                userDetails.getUser().getId(),
                userDetails.getUser().getRole()
        );

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CREATED, response));
    }

    @GetMapping("/stores/{storeId}/menu-categories")
    public ResponseEntity<ApiResponse<List<MenuCategoryResponse>>> getMenuCategories(
            @PathVariable UUID storeId
    ) {
        List<MenuCategoryResponse> responses = menuCategoryService.getMenuCategories(storeId);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SUCCESS, responses));
    }

    @PutMapping("/menu-categories/{menuCategoryId}")
    public ResponseEntity<ApiResponse<MenuCategoryResponse>> updateMenuCategory(
            @PathVariable UUID menuCategoryId,
            @Valid @RequestBody MenuCategoryUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        MenuCategoryResponse response = menuCategoryService.updateMenuCategory(
                menuCategoryId,
                request,
                userDetails.getUser().getId(),
                userDetails.getUser().getRole()
        );

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.UPDATED, response));
    }

    @PatchMapping("/stores/{storeId}/menu-categories/display-order")
    public ResponseEntity<ApiResponse<Void>> updateDisplayOrders(
            @PathVariable UUID storeId,
            @Valid @RequestBody MenuCategoryDisplayOrderUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
            ) {
        menuCategoryService.updateDisplayOrders(
                storeId,
                request.getMenuCategoryIds(),
                userDetails.getUser().getId(),
                userDetails.getUser().getRole()
        );
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.UPDATED));
    }

    @DeleteMapping("/menu-categories/{menuCategoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteMenuCategory(
            @PathVariable UUID menuCategoryId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        menuCategoryService.deleteMenuCategory(
                menuCategoryId,
                userDetails.getUser().getId(),
                userDetails.getUser().getRole()
        );

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.DELETED));
    }
}
