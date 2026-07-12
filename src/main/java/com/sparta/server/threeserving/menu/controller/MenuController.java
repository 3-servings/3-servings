package com.sparta.server.threeserving.menu.controller;

import com.sparta.server.threeserving.auth.UserDetailsImpl;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.menu.dto.request.MenuCreateRequest;
import com.sparta.server.threeserving.menu.dto.request.MenuUpdateRequest;
import com.sparta.server.threeserving.menu.dto.response.MenuBoardResponse;
import com.sparta.server.threeserving.menu.dto.response.MenuDetailResponse;
import com.sparta.server.threeserving.menu.dto.response.MenuResponse;
import com.sparta.server.threeserving.menu.entity.MenuStatus;
import com.sparta.server.threeserving.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/stores/{storeId}/menus")
    public ResponseEntity<ApiResponse<Page<MenuResponse>>> getMenus(
            @PathVariable UUID storeId,
            @RequestParam(required = false) MenuStatus status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<MenuResponse> responses = menuService.getMenus(
                storeId,
                status,
                keyword,
                pageable
        );

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SUCCESS, responses));
    }

    @GetMapping("/stores/{storeId}/menu-board")
    public ResponseEntity<ApiResponse<List<MenuBoardResponse.MenuBoardCategory>>> getMenuBoard(
            @PathVariable UUID storeId
    ) {
        List<MenuBoardResponse.MenuBoardCategory> responses = menuService.getMenuBoard(storeId);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SUCCESS, responses));
    }

    @GetMapping("/stores/{storeId}/menus/{menuId}")
    public ResponseEntity<ApiResponse<MenuDetailResponse>> getMenuDetail(
            @PathVariable UUID storeId,
            @PathVariable UUID menuId
    ) {
        MenuDetailResponse response = menuService.getMenuDetail(storeId, menuId);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SUCCESS, response));
    }

    @PutMapping("/menus/{menuId}")
    public ResponseEntity<ApiResponse<MenuResponse>> updateMenu(
            @PathVariable UUID menuId,
            @Valid @RequestBody MenuUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        MenuResponse response = menuService.updateMenu(
                menuId,
                request,
                userDetails.getUser().getId(),
                userDetails.getUser().getRole()
        );

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.UPDATED, response));
    }

    @DeleteMapping("/menus/{menuId}")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(
            @PathVariable UUID menuId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        menuService.deleteMenu(
                menuId,
                userDetails.getUser().getId(),
                userDetails.getUser().getRole()
        );

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.DELETED));
    }
}
