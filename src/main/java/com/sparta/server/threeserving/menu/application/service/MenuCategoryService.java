package com.sparta.server.threeserving.menu.application.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.menu.domain.entity.MenuCategory;
import com.sparta.server.threeserving.menu.domain.repository.MenuCategoryRepository;
import com.sparta.server.threeserving.menu.presentation.dto.request.MenuCategoryCreateRequest;
import com.sparta.server.threeserving.menu.presentation.dto.request.MenuCategoryUpdateRequest;
import com.sparta.server.threeserving.store.entity.Store;
import com.sparta.server.threeserving.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuCategoryService {

    private final MenuCategoryRepository menuCategoryRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public MenuCategory createMenuCategory(UUID storeId, MenuCategoryCreateRequest request) {
        // store 존재 여부 검증
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // menuCategory 증복 검증
        if (menuCategoryRepository.existsByStoreIdAndName(storeId, request.getName())) {
            throw new CustomException(ErrorCode.MENU_CATEGORY_NAME_DUPLICATED);
        }

        // menuCategory 생성
        MenuCategory menuCategory = MenuCategory.builder()
                .store(store)
                .name(request.getName())
                .displayOrder(request.getDisplayOrder())
                .build();

        // DB 저장
        return menuCategoryRepository.save(menuCategory);
    }

    @Transactional(readOnly = true)
    public List<MenuCategory> getMenuCategories(UUID storeId) {
        return menuCategoryRepository.findAllByStoreIdOrderByDisplayOrderAsc(storeId);
    }

    @Transactional
    public MenuCategory updateMenuCategory(UUID menuCategoryId, MenuCategoryUpdateRequest request) {
        // menuCategory 존재 여부 검증
        MenuCategory menuCategory = menuCategoryRepository.findById(menuCategoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_CATEGORY_NOT_FOUND));

        // menuCategory 이름을 변경할 때 -> menuCategory 이름 중복 검증
        if (menuCategoryRepository.existsByStoreIdAndNameAndIdNot(menuCategory.getStore().getId(), request.getName(), menuCategoryId)) {
            throw new CustomException(ErrorCode.MENU_CATEGORY_NAME_DUPLICATED);
        }

        // menuCategory 갱신
        menuCategory.update(request.getName(), request.getDisplayOrder());

        return menuCategory;
    }

    @Transactional
    public void deleteMenuCategory(UUID menuCategoryId, Long userId) {
        // menuCategory 존재 여부 검증
        MenuCategory menuCategory = menuCategoryRepository.findById(menuCategoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_CATEGORY_NOT_FOUND));

        // 기존 데이터 삭제여부 검증
        if (menuCategory.isDeleted()) {
            throw new CustomException(ErrorCode.MENU_CATEGORY_ALREADY_DELETED);
        }

        menuCategory.softDelete(userId);
    }
}
