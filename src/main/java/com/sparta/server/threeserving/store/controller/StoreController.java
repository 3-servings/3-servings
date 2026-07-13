package com.sparta.server.threeserving.store.controller;

import com.sparta.server.threeserving.auth.UserDetailsImpl;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.store.dto.StoreResponse;
import com.sparta.server.threeserving.store.dto.request.RegisterStore;
import com.sparta.server.threeserving.store.dto.request.StoreSearchCondition;
import com.sparta.server.threeserving.store.dto.request.UpdateStoreRequest;
import com.sparta.server.threeserving.store.service.StoreService;
import com.sparta.server.threeserving.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;



    @PostMapping("/")
    public ApiResponse<StoreResponse> registerStore(@Valid @RequestBody RegisterStore request, @AuthenticationPrincipal UserDetailsImpl userDetails){
        User user = userDetails.getUser();
        return storeService.registerStore(request, user);
    }

    @GetMapping("/")
    public ApiResponse<Page<StoreResponse>> getStores(
            StoreSearchCondition condition,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return storeService.getStores(condition, pageable);
    }

    @GetMapping("/{storeId}")
    public ApiResponse<StoreResponse> getStore(@PathVariable UUID storeId){
        return storeService.getStore(storeId);
    }

    @PutMapping("/{storeId}")
    public ApiResponse<StoreResponse> updateStore(
            @PathVariable UUID storeId,
            @Valid @RequestBody UpdateStoreRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        User owner = userDetails.getUser();
        return storeService.updateStore(storeId, request, owner);
    }

    @PatchMapping("/{storeId}/open")
    public ApiResponse<Void> openStore(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        Long userId = userDetails.getUser().getId();
        storeService.changeOpenStatus(storeId, userId, true);
        return ApiResponse.success(SuccessCode.UPDATED);
    }

    @PatchMapping("/{storeId}/close")
    public ApiResponse<Void> closeStore(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        storeService.changeOpenStatus(storeId, userId, false);
        return ApiResponse.success(SuccessCode.UPDATED);
    }

    @DeleteMapping("/{storeId}")
    public ApiResponse<Void> deleteStore(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        storeService.deleteStore(storeId, userId);
        return ApiResponse.success(SuccessCode.DELETED);
    }

}
