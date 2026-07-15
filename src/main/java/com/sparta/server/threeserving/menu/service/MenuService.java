package com.sparta.server.threeserving.menu.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.image.enums.DomainType;
import com.sparta.server.threeserving.image.service.ImageService;
import com.sparta.server.threeserving.menu.dto.request.*;
import com.sparta.server.threeserving.menu.dto.response.MenuBoardResponse;
import com.sparta.server.threeserving.menu.dto.response.MenuDetailResponse;
import com.sparta.server.threeserving.menu.dto.response.MenuResponse;
import com.sparta.server.threeserving.menu.entity.Menu;
import com.sparta.server.threeserving.menu.entity.MenuCategory;
import com.sparta.server.threeserving.menu.entity.MenuOptionGroup;
import com.sparta.server.threeserving.menu.entity.OptionGroup;
import com.sparta.server.threeserving.menu.enums.MenuStatus;
import com.sparta.server.threeserving.menu.repository.MenuCategoryRepository;
import com.sparta.server.threeserving.menu.repository.MenuRepository;
import com.sparta.server.threeserving.menu.repository.OptionGroupRepository;
import com.sparta.server.threeserving.store.entity.Store;
import com.sparta.server.threeserving.store.repository.StoreRepository;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final OptionGroupRepository optionGroupRepository;

    private final ImageService imageService;

    @Transactional
    public MenuResponse createMenu(UUID storeId, MenuCreateRequest request, Long userId, UserRoleEnum role) {
        // store 존재 여부 검증
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 사용자 권한 검증
        if (role != UserRoleEnum.MASTER && !store.getOwner().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // menuCategory 무결성 검증
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

        // 이미지 저장
        String imageUrl = imageService.saveImage(DomainType.MENU, savedMenu.getId(), request.getImage());

        return MenuResponse.from(savedMenu, imageUrl);
    }

    @Transactional(readOnly = true)
    public Page<MenuResponse> getMenus(UUID storeId, MenuStatus status, String keyword, Pageable pageable) {
        // store 존재 여부 검증
        if (!storeRepository.existsById(storeId)) {
            throw new CustomException(ErrorCode.STORE_NOT_FOUND);
        }

        // 동적 쿼리로 데이터 조회
        Page<Menu> menuPage = menuRepository.findMenusByCondition(storeId, status, keyword, pageable);

        List<UUID> menuIds = menuPage.getContent().stream().map(Menu::getId).toList();
        // 이미지 조회
        Map<UUID, String> imageUrlMap = imageService.getImageUrlMap(DomainType.MENU, menuIds);

        return menuPage.map(menu -> MenuResponse.from(menu, imageUrlMap.get(menu.getId())));
    }

    @Transactional(readOnly = true)
    public List<MenuBoardResponse.MenuBoardCategory> getMenuBoard(UUID storeId) {
        // store 존재 여부 검증
        if (!storeRepository.existsById(storeId)) {
            throw new CustomException(ErrorCode.STORE_NOT_FOUND);
        }

        // 고객 노출용 메뉴 상태값 필터링
        List<MenuStatus> targetStatuses = List.of(MenuStatus.AVAILABLE, MenuStatus.SOLD_OUT);

        List<Menu> menus = menuRepository.findMenusWithCategory(storeId, targetStatuses);
        List<UUID> menuIds = menus.stream().map(Menu::getId).toList();
        // 이미지 조회
        Map<UUID, String> imageUrlMap = imageService.getImageUrlMap(DomainType.MENU, menuIds);

        // 데이터 그룸핑
        Map<MenuCategory, List<Menu>> groupedMenus = menus.stream()
                .collect(Collectors.groupingBy(
                        Menu::getMenuCategory,
                        LinkedHashMap::new,     // LinkedHashMap으로 DB 정렬 순서 보장
                        Collectors.toList()
                ));

        return groupedMenus.entrySet().stream()
                .map(entry -> MenuBoardResponse.MenuBoardCategory.from(entry.getKey(), entry.getValue(), imageUrlMap))
                .toList();
    }

    @Transactional(readOnly = true)
    public MenuDetailResponse getMenuDetail(UUID menuId) {
        // 메뉴 상세 정보 조회 N+1 방어
        Menu menu = menuRepository.findMenuDetailById(menuId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

        // 이미지 조회
        String imageUrl = imageService.getImageUrl(DomainType.MENU, menuId);

        return MenuDetailResponse.from(menu, imageUrl);
    }

    @Transactional
    public MenuResponse updateMenu(UUID menuId, MenuUpdateRequest request, Long userId, UserRoleEnum role) {
        // menu 존재 여부 검증
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

        // 사용자 권한 검증
        if (role != UserRoleEnum.MASTER && !menu.getStore().getOwner().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // menu 이름이 변경된 경우, 이름 중복 검사
        if (!menu.getName().equals(request.getName())) {
            if (menuRepository.existsByStoreIdAndNameAndIdNot(menu.getStore().getId(), request.getName(), menuId)) {
                throw new CustomException(ErrorCode.MENU_NAME_DUPLICATED);
            }
        }

        // menuCategory 가 변경된 경우, 존재 여부 검증
        MenuCategory menuCategory = menu.getMenuCategory();
        if (!menuCategory.getId().equals(request.getMenuCategoryId())) {
            menuCategory = menuCategoryRepository.findById(request.getMenuCategoryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
        }

        // menu Update
        menu.update(
                menuCategory,
                request.getName(),
                request.getPrice(),
                request.getDescription(),
                request.getIsDescriptionAiGenerated(),
                request.getStatus()
        );

        // 이미지 교체
        String imageUrl = null;
        if (request.getImage() != null) {
            imageUrl = imageService.replaceImage(DomainType.MENU, menuId, request.getImage(), userId);
        } else {
            imageUrl = imageService.getImageUrl(DomainType.MENU, menuId);
        }

        return MenuResponse.from(menu, imageUrl);
    }

    @Transactional
    public void deleteMenu(UUID menuId, Long userId, UserRoleEnum role) {
        // menu 존재 여부 검증
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

        // 사용자 권한 검증
        if (role != UserRoleEnum.MASTER && !menu.getStore().getOwner().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        menu.softDelete(userId);

        // 이미지 soft delete
        imageService.softDeleteImages(DomainType.MENU, menuId, userId);

        // Menu - OptionGruop 매핑 테이블 hard delete
        menu.getMenuOptionGroups().clear();
    }

    @Transactional
    public void updateMenusStatus(UUID storeId, MenuStatusUpdateRequest request, Long userId, UserRoleEnum role) {
        // store 존재 여부 검증
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 사용자 권한 검증
        if (role != UserRoleEnum.MASTER && !store.getOwner().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        List<Menu> menus = menuRepository.findAllById(request.getMenuIds());

        // menu 존재 여부 검증
        if (menus.size() != request.getMenuIds().size()) {
            throw new CustomException(ErrorCode.MENU_NOT_FOUND);
        }

        for (Menu menu : menus) {
            // 다른 가게의 메뉴가 아닌지 검증
            if (!menu.getStore().getId().equals(storeId)) {
                throw new CustomException(ErrorCode.MENU_STORE_MISMATCH);
            }

            // Update
            menu.updateStatus(request.getStatus());
        }
    }

    @Transactional
    public void updateMenusDisplayOrder(UUID storeId, MenuDisplayOrderUpdateRequest request, Long userId, UserRoleEnum role) {

        List<UUID> menuIds = request.getMenuIds();
        UUID categoryId = request.getMenuCategoryId();

        // store 존재 여부 검증
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 사용자 권한 검증
        if (role != UserRoleEnum.MASTER && !store.getOwner().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        //  menu 존재 여부 검증
        List<Menu> menus = menuRepository.findAllById(menuIds);
        if (menus.size() != menuIds.size()) {
            throw new CustomException(ErrorCode.MENU_NOT_FOUND);
        }

        Map<UUID, Menu> menuMap = menus.stream()
                .collect(Collectors.toMap(Menu::getId, menu -> menu));

        // Update
        for (int i = 0; i < menuIds.size(); i++) {
            UUID targetId = menuIds.get(i);
            Menu targetMenu = menuMap.get(targetId);

            // 다른 가게의 메뉴가 아닌지 검증
            if (!targetMenu.getStore().getId().equals(storeId)) {
                throw new CustomException(ErrorCode.MENU_STORE_MISMATCH);
            }

            // 해당 메뉴 카테고리에 속한 메뉴가 맞는지 검증
            if (!targetMenu.getMenuCategory().getId().equals(categoryId)) {
                throw new CustomException(ErrorCode.MENU_MENU_CATEGORY_MISMATCH);
            }

            targetMenu.updateDisplayOrder(i + 1);
        }
    }

    @Transactional
    public void assignMenuOptionGroups(UUID menuId, MenuOptionGroupAssignRequest request, Long userId, UserRoleEnum role) {

        // menu 존재 여부 검증
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

        // 사용자 권한 검증
        if (role != UserRoleEnum.MASTER && !menu.getStore().getOwner().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        List<UUID> optionGroupIds = request.getOptionGroupIds();

        // 요청이 빈 배열인 경우 모든 옵션 그룹 연결 해제, hard delete
        if (optionGroupIds.isEmpty()) {
            menu.assignOptionGroups(new ArrayList<>());
            return;
        }

        // optionGroup 존재 여부 검증
        List<OptionGroup> optionGroups = optionGroupRepository.findAllById(optionGroupIds);
        if (optionGroups.size() != optionGroupIds.size()) {
            throw new CustomException(ErrorCode.OPTION_GROUP_NOT_FOUND);
        }

        Map<UUID, OptionGroup> optionGroupMap = optionGroups.stream()
                .collect(Collectors.toMap(OptionGroup::getId, og -> og));

        // 새로운 매핑 리스트 생성
        List<MenuOptionGroup> newMappings = new ArrayList<>();

        for (int i = 0; i < optionGroupIds.size(); i++) {
            UUID targetId = optionGroupIds.get(i);
            OptionGroup targetOptionGroup = optionGroupMap.get(targetId);

            // 다른 가게의 옵션 그룹을 연결하려는지 검증
            if (!targetOptionGroup.getStore().getId().equals(menu.getStore().getId())) {
                throw new CustomException(ErrorCode.OPTION_GROUP_STORE_MISMATCH);
            }

            newMappings.add(MenuOptionGroup.builder()
                    .menu(menu)
                    .optionGroup(targetOptionGroup)
                    .displayOrder(i + 1)
                    .build());
        }

        // 엔티티 상태 동기화 (영속성 컨텍스트가 알아서 DELETE 후 INSERT 수행)
        menu.assignOptionGroups(newMappings);
    }
}
