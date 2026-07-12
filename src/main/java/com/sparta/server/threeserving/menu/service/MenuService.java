package com.sparta.server.threeserving.menu.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.menu.dto.request.MenuCreateRequest;
import com.sparta.server.threeserving.menu.dto.response.MenuResponse;
import com.sparta.server.threeserving.menu.entity.Menu;
import com.sparta.server.threeserving.menu.entity.MenuCategory;
import com.sparta.server.threeserving.menu.repository.MenuCategoryRepository;
import com.sparta.server.threeserving.menu.repository.MenuRepository;
import com.sparta.server.threeserving.store.entity.Store;
import com.sparta.server.threeserving.store.repository.StoreRepository;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final MenuCategoryRepository menuCategoryRepository;

    @Transactional
    public MenuResponse createMenu(UUID storeId, MenuCreateRequest request, Long userId, UserRoleEnum role) {
        // store 존재 여부 검증
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 사용자 권한 검증
        if (role != UserRoleEnum.MASTER && !store.getOwner().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 메뉴 카테고리 무결성 검증
        MenuCategory menuCategory = menuCategoryRepository.findById(request.getMenuCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_CATEGORY_NOT_FOUND));

        // 다른 가게의 카테고리가 아닌지 검증
        if (!menuCategory.getStore().getId().equals(storeId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 메뉴 이름 중복 검증
        if (menuRepository.existsByStoreIdAndName(storeId, request.getName())) {
            throw new CustomException(ErrorCode.MENU_NAME_DUPLICATED);
        }

        // 신규 메뉴 노출 순서는 최후순위로 설정 -> 메뉴 생성 이후 노출 순서 변경 API로 수정
        int maxDisplayOrder = menuRepository.findMaxDisplayOrderByMenuCategoryId(menuCategory.getId());
        int nextDisplayOrder = maxDisplayOrder + 1;

        // 메뉴 생성
        Menu menu = Menu.builder()
                .store(store)
                .menuCategory(menuCategory)
                .name(request.getName())
                .price(request.getPrice())
                .description(request.getDescription())
                .isDescriptionAiGenerated(request.isDescriptionAiGenerated())
                .displayOrder(nextDisplayOrder)
                .build();

        // DB 저장
        Menu savedMenu = menuRepository.save(menu);

        return MenuResponse.from(savedMenu);
    }

}
