package com.sparta.server.threeserving.menu.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.menu.dto.request.MenuCategoryCreateRequest;
import com.sparta.server.threeserving.menu.dto.request.MenuCategoryDisplayOrderUpdateRequest;
import com.sparta.server.threeserving.menu.dto.request.MenuCategoryUpdateRequest;
import com.sparta.server.threeserving.menu.dto.response.MenuCategoryResponse;
import com.sparta.server.threeserving.menu.entity.MenuCategory;
import com.sparta.server.threeserving.menu.repository.MenuCategoryRepository;
import com.sparta.server.threeserving.menu.repository.MenuRepository;
import com.sparta.server.threeserving.store.entity.Store;
import com.sparta.server.threeserving.store.repository.StoreRepository;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuCategoryService {

    private final MenuCategoryRepository menuCategoryRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;

    @Transactional
    public MenuCategoryResponse createMenuCategory(UUID storeId, MenuCategoryCreateRequest request, Long userId, UserRoleEnum role) {
        // store 존재 여부 검증
        Store store = storeRepository.findByIdWithOwner(storeId)
                .orElseThrow(() -> {
                    log.warn("Store not found - StoreId: {}", storeId);
                    return new CustomException(ErrorCode.STORE_NOT_FOUND);
                });

        // 사용자 권한 검증
        if (role != UserRoleEnum.MASTER && !store.getOwner().getId().equals(userId)) {
            log.warn("Access Denied (Create) - UserId: {}, StoreId: {}", userId, storeId);
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // menuCategory 이름 증복 검증
        if (menuCategoryRepository.existsByStoreIdAndName(storeId, request.getName())) {
            log.warn("Menu Category name duplicated - StoreId: {}, Name: {}", storeId, request.getName());
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
        log.info("Menu Category created - StoreId: {}, CategoryId: {}, Name: {}", storeId, savedCategory.getId(), savedCategory.getName());

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
        MenuCategory menuCategory = menuCategoryRepository.findByIdWithStoreAndOwner(menuCategoryId)
                .orElseThrow(() -> {
                    log.warn("Menu Category not found - CategoryId: {}", menuCategoryId);
                    return new CustomException(ErrorCode.MENU_CATEGORY_NOT_FOUND);
                });
        // 사용자 권한 검증
        if (role != UserRoleEnum.MASTER && !menuCategory.getStore().getOwner().getId().equals(userId)) {
            log.warn("Access Denied (Update) - UserId: {}, CategoryId: {}", userId, menuCategoryId);
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // menuCategory 이름을 변경할 때 -> menuCategory 이름 중복 검증
        if (menuCategoryRepository.existsByStoreIdAndNameAndIdNot(menuCategory.getStore().getId(), request.getName(), menuCategoryId)) {
            log.warn("Menu Category name duplicated - StoreId: {}, CategoryId: {}, RequestName: {}", menuCategory.getStore().getId(), menuCategoryId, request.getName());
            throw new CustomException(ErrorCode.MENU_CATEGORY_NAME_DUPLICATED);
        }

        // Update (name 만 수정 가능, displayOrder는 순서 수정 API 사용)
        String oldName = menuCategory.getName();
        menuCategory.update(request.getName());
        log.info("Menu Category name updated - CategoryId: {}, OldName: {}, NewName: {}", menuCategoryId, oldName, request.getName());

        return MenuCategoryResponse.from(menuCategory);
    }

    @Transactional
    public void updateDisplayOrders(UUID storeId, MenuCategoryDisplayOrderUpdateRequest request, Long userId, UserRoleEnum role) {

        List<UUID> menuCategoryIds = request.getMenuCategoryIds();

        // store 존재 여부 검증
        Store store = storeRepository.findByIdWithOwner(storeId)
                .orElseThrow(() -> {
                    log.warn("Store not found - StoreId: {}", storeId);
                    return new CustomException(ErrorCode.STORE_NOT_FOUND);
                });

        // 사용자 권한 검증
        if (role != UserRoleEnum.MASTER && !store.getOwner().getId().equals(userId)) {
            log.warn("Access Denied (Update Orders) - UserId: {}, StoreId: {}", userId, storeId);
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // menuCategory 존재 여부 검증
        List<MenuCategory> menuCategories = menuCategoryRepository.findAllByIdInWithStore(menuCategoryIds);
        if (menuCategories.size() != menuCategoryIds.size()) {
            log.warn("Menu Category count mismatch - StoreId: {}, Expected: {}, Found: {}", storeId, menuCategoryIds.size(), menuCategories.size());
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
                log.warn("Access Denied (Cross-Store) - UserId: {}, TargetCategoryId: {}, StoreId: {}", userId, targetId, storeId);
                throw new CustomException(ErrorCode.ACCESS_DENIED);
            }

            targetCategory.updateDisplayOrder(i + 1);
        }

        log.info("Menu Category display orders updated - StoreId: {}, UpdatedCount: {}", storeId, menuCategoryIds.size());
    }

    @Transactional
    public void deleteMenuCategory(UUID menuCategoryId, Long userId, UserRoleEnum role) {
        // menuCategory 존재 여부 검증
        MenuCategory menuCategory = menuCategoryRepository.findByIdWithStoreAndOwner(menuCategoryId)
                .orElseThrow(() -> {
                    log.warn("Menu Category not found - CategoryId: {}", menuCategoryId);
                    return new CustomException(ErrorCode.MENU_CATEGORY_NOT_FOUND);
                });

        // 권한 검증
        if (role != UserRoleEnum.MASTER && !menuCategory.getStore().getOwner().getId().equals(userId)) {
            log.warn("Access Denied (Delete) - UserId: {}, CategoryId: {}", userId, menuCategoryId);
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 메뉴 카테고리 하위에 메뉴가 존재하면 삭제 방지
        if (menuRepository.existsByMenuCategoryIdAndDeletedAtIsNull(menuCategoryId)) {
            log.warn("Menu Category deletion blocked (Active menus exist) - CategoryId: {}", menuCategoryId);
            throw new CustomException(ErrorCode.MENU_CATEGORY_HAS_MENUS);
        }

        menuCategory.softDelete(userId);
        log.info("Menu Category deleted - CategoryId: {}, DeletedByUserId: {}", menuCategoryId, userId);
    }
}
