package com.sparta.server.threeserving.menu.dto.response;

import com.sparta.server.threeserving.menu.entity.OptionGroup;
import com.sparta.server.threeserving.menu.entity.OptionItem;
import com.sparta.server.threeserving.menu.enums.OptionItemStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class OptionGroupResponse {
    private final UUID id;
    private final String name;
    private final int minSelect;
    private final int maxSelect;
    private final List<OptionItemResponse> optionItems;

    public static OptionGroupResponse from(OptionGroup optionGroup) {
        return OptionGroupResponse.builder()
                .id(optionGroup.getId())
                .name(optionGroup.getName())
                .minSelect(optionGroup.getMinSelect())
                .maxSelect(optionGroup.getMaxSelect())
                .optionItems(optionGroup.getOptionItemList().stream()
                        .map(OptionItemResponse::from)
                        .toList())
                .build();
    }

    @Getter
    @Builder
    public static class OptionItemResponse {
        private final UUID id;
        private final String name;
        private final int price;
        private final int displayOrder;
        private final OptionItemStatus status;

        public static OptionItemResponse from(OptionItem optionItem) {
            return OptionItemResponse.builder()
                    .id(optionItem.getId())
                    .name(optionItem.getName())
                    .price(optionItem.getPrice())
                    .displayOrder(optionItem.getDisplayOrder())
                    .status(optionItem.getStatus())
                    .build();
        }
    }
}