package com.sparta.server.threeserving.menu.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.menu.dto.response.MenuCategoryResponse;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuCategoryService {

    private final MenuCategoryRepository menuCategoryRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public MenuCategoryResponse createMenuCategory(UUID storeId, MenuCategoryCreateRequest request, Long userId, UserRoleEnum role) {
        // store 존재 여부 검증
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 사용자 권한 검증
        if (role != UserRoleEnum.MASTER && !store.getOwner().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // menuCategory 이름 증복 검증
        if (menuCategoryRepository.existsByStoreIdAndName(storeId, request.getName())) {
            throw new CustomException(ErrorCode.MENU_CATEGORY_NAME_DUPLICATED);
        }

        int maxDisplayOrder = menuCategoryRepository.findMaxDisplayOrder(storeId);
        int nextDisplayOrder = maxDisplayOrder + 1;

        MenuCategory menuCategory = MenuCategory.builder()
                .store(store)
                .name(request.getName())
                .displayOrder(nextDisplayOrder)
                .build();

        // 저장
        MenuCategory savedCategory = menuCategoryRepository.save(menuCategory);

        return MenuCategoryResponse.from(savedCategory);
    }

    @Transactional(readOnly = true)
    public List<MenuCategoryResponse> getMenuCategories(UUID storeId) {
        return menuCategoryRepository.findAllByStoreIdOrderByDisplayOrderAsc(storeId)
                .stream()
                .map(MenuCategoryResponse::from)
                .toList();
    }

    @Transactional
    public MenuCategoryResponse updateMenuCategory(UUID menuCategoryId, MenuCategoryUpdateRequest request, Long userId, UserRoleEnum role) {
        // menuCategory 존재 여부 검증
        MenuCategory menuCategory = menuCategoryRepository.findById(menuCategoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_CATEGORY_NOT_FOUND));

        // 사용자 권한 검증
        if (role != UserRoleEnum.MASTER && !menuCategory.getStore().getOwner().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // menuCategory 이름을 변경할 때 -> menuCategory 이름 중복 검증
        if (menuCategoryRepository.existsByStoreIdAndNameAndIdNot(menuCategory.getStore().getId(), request.getName(), menuCategoryId)) {
            throw new CustomException(ErrorCode.MENU_CATEGORY_NAME_DUPLICATED);
        }

        // Update (name 만 수정 가능, displayOrder는 순서 수정 API 사용)
        menuCategory.update(request.getName());

        return MenuCategoryResponse.from(menuCategory);
    }

    @Transactional
    public void updateDisplayOrders(UUID storeId, List<UUID> menuCategoryIds, Long userId, UserRoleEnum role) {
        // store 존재 여부 검증
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 사용자 권한 검증
        if (role != UserRoleEnum.MASTER && !store.getOwner().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // menuCategory 존재 여부 검증
        List<MenuCategory> menuCategories = menuCategoryRepository.findAllById(menuCategoryIds);
        if (menuCategories.size() != menuCategoryIds.size()) {
            throw new CustomException(ErrorCode.MENU_CATEGORY_NOT_FOUND);
        }

        Map<UUID, MenuCategory> menuCategoryMap = menuCategories.stream()
                .collect(Collectors.toMap(MenuCategory::getId, menuCategory -> menuCategory));

        // Update displayOrder
        for (int i = 0; i < menuCategoryIds.size(); i++) {
            UUID targetId = menuCategoryIds.get(i);

            MenuCategory targetCategory = menuCategoryMap.get(targetId);

            // 다른 가게의 카테고리가 아닌지 검증
            if (!targetCategory.getStore().getId().equals(storeId)) {
                throw new CustomException(ErrorCode.ACCESS_DENIED);
            }

            targetCategory.updateDisplayOrder(i + 1);
        }
    }

    @Transactional
    public void deleteMenuCategory(UUID menuCategoryId, Long userId, UserRoleEnum role) {
        // menuCategory 존재 여부 검증
        MenuCategory menuCategory = menuCategoryRepository.findById(menuCategoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_CATEGORY_NOT_FOUND));

        // 권한 검증
        if (role != UserRoleEnum.MASTER && !menuCategory.getStore().getOwner().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        menuCategory.softDelete(userId);
    }
}
