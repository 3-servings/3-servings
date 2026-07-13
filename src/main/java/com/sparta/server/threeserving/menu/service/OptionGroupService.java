package com.sparta.server.threeserving.menu.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.menu.dto.request.OptionGroupCreateRequest;
import com.sparta.server.threeserving.menu.dto.request.OptionGroupUpdateRequest;
import com.sparta.server.threeserving.menu.dto.response.OptionGroupResponse;
import com.sparta.server.threeserving.menu.entity.OptionGroup;
import com.sparta.server.threeserving.menu.entity.OptionItem;
import com.sparta.server.threeserving.menu.repository.OptionGroupRepository;
import com.sparta.server.threeserving.store.entity.Store;
import com.sparta.server.threeserving.store.repository.StoreRepository;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OptionGroupService {

    private final OptionGroupRepository optionGroupRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public OptionGroupResponse createOptionGroup(UUID storeId, OptionGroupCreateRequest request, Long userId, UserRoleEnum role) {
        // minSelect, maxSelect 유효성 검증
        int itemSize = request.getOptionItems().size();
        if (request.getMinSelect() > request.getMaxSelect() || request.getMinSelect() > itemSize || request.getMaxSelect() > itemSize) {
            throw new CustomException(ErrorCode.INVALID_OPTION_SELECTION);
        }

        // store 존재 여부 검증
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 사용자 권한 검증
        if (role != UserRoleEnum.MASTER && !store.getOwner().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // optionGroup 이름 중복 검증
        if (optionGroupRepository.existsByStoreIdAndName(storeId, request.getName())) {
            throw new CustomException(ErrorCode.OPTION_GROUP_NAME_DUPLICATED);
        }

        // optionGroup 엔티티 생성
        OptionGroup optionGroup = OptionGroup.builder()
                .store(store)
                .name(request.getName())
                .minSelect(request.getMinSelect())
                .maxSelect(request.getMaxSelect())
                .build();

        // optionItem 리스트 생성 및 연결
        for (int i = 0; i < request.getOptionItems().size(); i++) {
            var itemReq = request.getOptionItems().get(i);
            OptionItem item = OptionItem.builder()
                    .optionGroup(optionGroup)
                    .name(itemReq.getName())
                    .price(itemReq.getPrice())
                    .displayOrder(i + 1)
                    .build();

            optionGroup.addOptionItem(item);
        }

        // DB 저장, Cascade 설정으로 item도 함께 저장
        OptionGroup savedOptionGroup = optionGroupRepository.save(optionGroup);

        return OptionGroupResponse.from(savedOptionGroup);
    }

    @Transactional(readOnly = true)
    public Page<OptionGroupResponse> getOptionGroups(UUID storeId, String keyword, Pageable pageable) {
        // store 존재 여부 검증
        if (!storeRepository.existsById(storeId)) {
            throw new CustomException(ErrorCode.STORE_NOT_FOUND);
        }

        Page<OptionGroup> optionGroups;
        // 키워드 분기 처리
        if (keyword != null && !keyword.isBlank()) {
            optionGroups = optionGroupRepository.findByStoreIdAndNameContainingIgnoreCase(storeId, keyword, pageable);
        } else {
            optionGroups = optionGroupRepository.findAllByStoreId(storeId, pageable);
        }

        // LazyInitializationException 방지를 위해 service 에서 dto 변환
        return optionGroups.map(OptionGroupResponse::from);
    }

    // Delta Update / Upsert 방식
    @Transactional
    public OptionGroupResponse updateOptionGroup(UUID optionGroupId, OptionGroupUpdateRequest request, Long userId, UserRoleEnum role) {
        // minSelect, maxSelect 유효성 검증
        int itemSize = request.getOptionItems().size();
        if (request.getMinSelect() > request.getMaxSelect() || request.getMinSelect() > itemSize || request.getMaxSelect() > itemSize) {
            throw new CustomException(ErrorCode.INVALID_OPTION_SELECTION);
        }

        // optionGroup 존재 여부 검증
        OptionGroup optionGroup = optionGroupRepository.findById(optionGroupId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPTION_GROUP_NOT_FOUND));

        // 사용자 권한 검증
        if (role != UserRoleEnum.MASTER && !optionGroup.getStore().getOwner().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // optionGroup 이름 중복 검사 (본인 제외)
        if (optionGroupRepository.existsByStoreIdAndNameAndIdNot(optionGroup.getStore().getId(), request.getName(), optionGroupId)) {
            throw new CustomException(ErrorCode.OPTION_GROUP_NAME_DUPLICATED);
        }

        // optionGroup Update
        optionGroup.update(request.getName(), request.getMinSelect(), request.getMaxSelect());

        // optionItem Delta Update
        List<OptionGroupUpdateRequest.OptionItemRequest> optionItemRequests = request.getOptionItems();
        // 1. 요청 DTO에 포함된 기존 아이템의 ID 목록 추출 (새로 추가될 null ID는 제외)
        Set<UUID> requestedOptionItemIds = optionItemRequests.stream()
                .map(OptionGroupUpdateRequest.OptionItemRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 2. DB에는 있지만 업데이트 목록에는 없는 아이템 삭제
        optionGroup.getOptionItemList().removeIf(item -> !requestedOptionItemIds.contains(item.getId()));

        // 3. Update & Insert
        int displayOrder = 1;
        for (var optionItemReq : optionItemRequests) {
            if (optionItemReq.getId() != null) {
                // id가 있으므로 기존 아이템 -> 수정
                OptionItem existingOptionItem = optionGroup.getOptionItemList().stream()
                        .filter(item -> item.getId().equals(optionItemReq.getId()))       // 같은
                        .findFirst()                                                                //
                        .orElseThrow(() -> new CustomException(ErrorCode.OPTION_ITEM_NOT_FOUND));   // 없는 아이템 수정 시도시 에러

                existingOptionItem.update(optionItemReq.getName(), optionItemReq.getPrice(), displayOrder++);
            } else {
                // id가 null 이므로 신규 아이템 -> 추가
                OptionItem newOptionItem = OptionItem.builder()
                        .optionGroup(optionGroup)
                        .name(optionItemReq.getName())
                        .price(optionItemReq.getPrice())
                        .displayOrder(displayOrder++)
                        .build();
                optionGroup.addOptionItem(newOptionItem);
            }
        }

        return OptionGroupResponse.from(optionGroup);
    }

    @Transactional
    public void deleteOptionGroup(UUID optionGroupId, Long userId, UserRoleEnum role) {
        // optionGroup 존재 여부 검증
        OptionGroup optionGroup = optionGroupRepository.findById(optionGroupId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPTION_GROUP_NOT_FOUND));

        // 권한 검증
        if (role != UserRoleEnum.MASTER && !optionGroup.getStore().getOwner().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        optionGroup.softDelete(userId);
    }
}
