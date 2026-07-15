package com.sparta.server.threeserving.store.controller;

import com.sparta.server.threeserving.auth.UserDetailsImpl;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.store.dto.request.CategoryRequest;
import com.sparta.server.threeserving.store.dto.response.CategoryResponse;
import com.sparta.server.threeserving.store.service.CategoryService;
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
@RequiredArgsConstructor
@RequestMapping("/api/categorys")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request){
        return categoryService.createCategory(request);
    }

    @PutMapping("/{categoryId}")
    public ApiResponse<CategoryResponse> updateCategory(@PathVariable UUID categoryId, @Valid @RequestBody CategoryRequest request){
        return categoryService.updateCategory(request, categoryId);
    }

    @PatchMapping("/{categoryId}/on-active")
    public ApiResponse<Void> activeOn(@PathVariable UUID categoryId){
        return categoryService.activeOn(categoryId);
    }

    @PatchMapping("/{categoryId}/off-active")
    public ApiResponse<Void> activeOff(@PathVariable UUID categoryId){
        return categoryService.activeOff(categoryId);
    }

    @GetMapping("/{categoryId}")
    public ApiResponse<CategoryResponse> getCategory(@PathVariable UUID categoryId){
        return categoryService.getCategory(categoryId);
    }

    @GetMapping
    public ApiResponse<Page<CategoryResponse>> searchCategories(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "true") Boolean isActive,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable
            ){
        return categoryService.searchCategories(name,isActive, pageable);
    }

    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> deleteCategory(
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
            ){
        Long userId = userDetails.getUser().getId();
        return categoryService.deleteCategory(categoryId, userId);
    }
}
