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
import org.springframework.security.access.prepost.PreAuthorize;
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
//    @PreAuthorize("hasRole('OWNER')") // todo: security 활성화 이후 반영
    public ResponseEntity<ApiResponse<MenuCategoryResponse>> createMenuCategory(
            @PathVariable UUID storeId,
            @Valid @RequestBody MenuCategoryCreateRequest request
    ) {
        MenuCategoryResponse response = MenuCategoryResponse.from(
                menuCategoryService.createMenuCategory(storeId, request)
        );

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CREATED, response));
    }

    @GetMapping("/stores/{storeId}/menu-categories")
    public ResponseEntity<ApiResponse<List<MenuCategoryResponse>>> getMenuCategories(
            @PathVariable UUID storeId
    ) {
        List<MenuCategoryResponse> responses = menuCategoryService.getMenuCategories(storeId)
                .stream().map(MenuCategoryResponse::from).toList();

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SUCCESS, responses));
    }

    @PutMapping("/menu-categories/{menuCategoryId}")
//    @PreAuthorize("hasRole('OWNER')") // todo: security 활성화 이후 반영
    public ResponseEntity<ApiResponse<MenuCategoryResponse>> updateMenuCategory(
            @PathVariable UUID menuCategoryId,
            @Valid @RequestBody MenuCategoryUpdateRequest request
    ) {
        MenuCategoryResponse response = MenuCategoryResponse.from(
                menuCategoryService.updateMenuCategory(menuCategoryId, request)
        );

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.UPDATED, response));
    }

    @PatchMapping("/stores/{storeId}/menu-categories/display-order")
    public ResponseEntity<ApiResponse<Void>> updateDisplayOrders(
            @PathVariable UUID storeId,
            @Valid @RequestBody MenuCategoryDisplayOrderUpdateRequest request
            ) {
        menuCategoryService.updateDisplayOrders(storeId, request.getCategoryIds());

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.UPDATED));
    }

    @DeleteMapping("/menu-categories/{menuCategoryId}")
//    @PreAuthorize("hasRole('OWNER')") // todo: security 활성화 이후 반영
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
