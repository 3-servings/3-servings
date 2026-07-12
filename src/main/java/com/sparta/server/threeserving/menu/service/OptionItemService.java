package com.sparta.server.threeserving.menu.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.menu.dto.request.OptionItemStatusUpdateRequest;
import com.sparta.server.threeserving.menu.entity.OptionGroup;
import com.sparta.server.threeserving.menu.entity.OptionItem;
import com.sparta.server.threeserving.menu.entity.OptionItemStatus;
import com.sparta.server.threeserving.menu.repository.OptionItemRepository;
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
public class OptionItemService {

    private final OptionItemRepository optionItemRepository;

    @Transactional
    public void updateOptionItemsStatus(OptionItemStatusUpdateRequest request, Long userId, UserRoleEnum role) {

        Map<UUID, OptionItemStatus> statusUpdateMap = request.getItemStatusUpdates().stream()
                .collect(Collectors.toMap(
                        OptionItemStatusUpdateRequest.ItemStatusUpdate::getOptionItemId,
                        OptionItemStatusUpdateRequest.ItemStatusUpdate::getStatus,
                        (oldValue, newValue) -> newValue
                ));

        // Fetch Join 사용하여 N+1 방어
        List<OptionItem> optionItems = optionItemRepository.findAllWithGroupAndStoreByIdIn(statusUpdateMap.keySet());

        // optionItem 존재 여부 검증
        if (optionItems.size() != statusUpdateMap.size()) {
            throw new CustomException(ErrorCode.OPTION_ITEM_NOT_FOUND);
        }

        // status 변경에 따른 optionGroup의 minSelect 위반 검증
        // 선택 가능한 옵션 개수를 minSelect 값보다 작은 값으로 수정하려면 에러 응답 -> minSelect 변경 유도
        Map<OptionGroup, List<OptionItem>> itemsByGroup = optionItems.stream()
                .collect(Collectors.groupingBy(OptionItem::getOptionGroup));

        for (var entry : itemsByGroup.entrySet()) {
            OptionGroup group = entry.getKey();
            List<OptionItem> changingItems = entry.getValue();

            // 사용자 권한 검증
            if (role != UserRoleEnum.MASTER && !group.getStore().getOwner().getId().equals(userId)) {
                throw new CustomException(ErrorCode.ACCESS_DENIED);
            }

            if (group.getMinSelect() > 0) {
                // DB상 현재 판매 중(AVAILABLE)인 아이템 개수
                long currentAvailableCount = group.getOptionItemList().stream()
                        .filter(i -> i.getStatus() == OptionItemStatus.AVAILABLE)
                        .count();

                long netChange = 0; // AVAILABLE 개수 변동

                for (OptionItem item : changingItems) {
                    OptionItemStatus oldStatus = item.getStatus();
                    OptionItemStatus newStatus = statusUpdateMap.get(item.getId());

                    if (oldStatus == OptionItemStatus.AVAILABLE && newStatus != OptionItemStatus.AVAILABLE) {
                        netChange--; // AVAILABLE -> 다른 status 로 변경시 --
                    } else if (oldStatus != OptionItemStatus.AVAILABLE && newStatus == OptionItemStatus.AVAILABLE) {
                        netChange++; // 다른 status -> AVAILABLE 변경시 ++
                    }
                }

                // 변경 후 최종 판매 중(AVAILABLE) 개수가 최소 조건보다 적어지면 예외 발생
                long updatedAvailableCount = currentAvailableCount + netChange;
                if (updatedAvailableCount < group.getMinSelect()) {
                    throw new CustomException(ErrorCode.OPTION_MIN_SELECT_VIOLATION);
                }
            }
        }

        // Update
        for (OptionItem item : optionItems) {
            OptionItemStatus newStatus = statusUpdateMap.get(item.getId());
            item.updateStatus(newStatus);
        }
    }
}
