package com.sparta.server.threeserving.store.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.store.dto.request.CategoryRequest;
import com.sparta.server.threeserving.store.dto.response.CategoryResponse;
import com.sparta.server.threeserving.store.entity.Category;
import com.sparta.server.threeserving.store.repository.CategoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public ApiResponse<CategoryResponse> createCategory(@Valid CategoryRequest request) {
        if(categoryRepository.existsByName(request.name())){
            throw new CustomException(ErrorCode.DUPLICATED_RESOURCE);
        }
        Category category = Category.builder()
                .name(request.name())
                .build();

        return ApiResponse.success(SuccessCode.CREATED, new CategoryResponse(categoryRepository.save(category)));
    }

    @Transactional
    public ApiResponse<CategoryResponse> updateCategory(@Valid CategoryRequest request, UUID categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND)
        );

        String name = request.name();

        if(categoryRepository.existsByName(name)){
            throw new CustomException(ErrorCode.DUPLICATED_RESOURCE);
        }

        category.changeName(name);

        return ApiResponse.success(SuccessCode.SUCCESS, new CategoryResponse(category));
    }


    public ApiResponse<CategoryResponse> getCategory(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND)
        );

        return ApiResponse.success(SuccessCode.SUCCESS, new CategoryResponse(category));
    }


    @Transactional
    public ApiResponse<Void> deleteCategory(UUID categoryId, Long userId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND)
        );

        category.softDelete(userId);

        return ApiResponse.success(SuccessCode.DELETED);
    }

    @Transactional
    public ApiResponse<Void> activeOn(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND)
        );

        category.changeIsActive(true);

        return ApiResponse.success(SuccessCode.UPDATED);
    }

    @Transactional
    public ApiResponse<Void> activeOff(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND)
        );

        category.changeIsActive(false);

        return ApiResponse.success(SuccessCode.UPDATED);
    }

    public ApiResponse<Page<CategoryResponse>> getCategories(Pageable pageable) {
        Pageable newPageable = PageRequest.of(pageable.getPageNumber(),PageService.resolvePageSize(pageable.getPageSize()),  pageable.getSort());
        Page<CategoryResponse> categories = categoryRepository.findByIsActive(true, newPageable).map(CategoryResponse::new);
        return ApiResponse.success(SuccessCode.SUCCESS, categories);
    }




}
