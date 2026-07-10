package com.sparta.server.threeserving.menu.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.menu.entity.MenuCategory;
import com.sparta.server.threeserving.menu.repository.MenuCategoryRepository;
import com.sparta.server.threeserving.menu.dto.request.MenuCategoryCreateRequest;
import com.sparta.server.threeserving.menu.dto.request.MenuCategoryUpdateRequest;
import com.sparta.server.threeserving.store.entity.Store;
import com.sparta.server.threeserving.store.repository.StoreRepository;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
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

        // displayOrder를 1 또는 max 값 + 1 처리
        Integer maxDisplayOrder = menuCategoryRepository.findMaxDisplayOrder(storeId);
        int nextDisplayOrder = maxDisplayOrder + 1;

        // menuCategory 생성
        MenuCategory menuCategory = MenuCategory.builder()
                .store(store)
                .name(request.getName())
                .displayOrder(nextDisplayOrder)
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

        // menuCategory 갱신 (name 만 수정 가능, displayOrder는 순서 수정 API 사용)
        menuCategory.update(request.getName());

        return menuCategory;
    }

    @Transactional
    public void updateDisplayOrders(UUID storeId, List<UUID> categoryIds) {
        for (int i = 0; i < categoryIds.size(); i++) {
            MenuCategory menuCategory = menuCategoryRepository.findById(categoryIds.get(i))
                    .orElseThrow(() -> new CustomException(ErrorCode.MENU_CATEGORY_NOT_FOUND));

            if (!menuCategory.getStore().getId().equals(storeId)) {
                throw new CustomException(ErrorCode.ACCESS_DENIED);
            }

            menuCategory.updateDisplayOrder(i + 1); // 1부터 시작하여 순서대로 할당
        }
    }

    @Transactional
    public void deleteMenuCategory(UUID menuCategoryId, Long userId, UserRoleEnum role) {
        // menuCategory 존재 여부 검증
        MenuCategory menuCategory = menuCategoryRepository.findById(menuCategoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_CATEGORY_NOT_FOUND));

        // 요청한 사용자가 운영자이거나 그 가게의 주인인지 확인
        if (role != UserRoleEnum.MASTER && !menuCategory.getStore().getOwner().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 기존 데이터 삭제여부 검증
        if (menuCategory.isDeleted()) {
            throw new CustomException(ErrorCode.MENU_CATEGORY_ALREADY_DELETED);
        }

        menuCategory.softDelete(userId);
    }
}
