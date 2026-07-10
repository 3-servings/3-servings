package com.sparta.server.threeserving.menu.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.menu.dto.request.OptionGroupCreateRequest;
import com.sparta.server.threeserving.menu.dto.response.OptionGroupResponse;
import com.sparta.server.threeserving.menu.entity.OptionGroup;
import com.sparta.server.threeserving.menu.entity.OptionItem;
import com.sparta.server.threeserving.menu.repository.OptionGroupRepository;
import com.sparta.server.threeserving.store.entity.Store;
import com.sparta.server.threeserving.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OptionGroupService {

    private final OptionGroupRepository optionGroupRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public OptionGroup createOptionGroup(UUID storeId, OptionGroupCreateRequest request) {

        // 옵션 개수 설정 비즈니스 검증: minSelect > maxSelect
        if (request.getMinSelect() > request.getMaxSelect()) {
            throw new CustomException(ErrorCode.INVALID_OPTION_SELECTION);
        }

        // optionGroup 이름 중복 검증
        if (optionGroupRepository.existsByStoreIdAndName(storeId, request.getName())) {
            throw new CustomException(ErrorCode.OPTION_GROUP_NAME_DUPLICATED);
        }

        // store 존재 여부 검증
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

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
        return optionGroupRepository.save(optionGroup);
    }

    @Transactional(readOnly = true)
    public Page<OptionGroupResponse> getOptionGroups(UUID storeId, String keyword, Pageable pageable) {
        // 가게 존재 여부 검증
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

}
