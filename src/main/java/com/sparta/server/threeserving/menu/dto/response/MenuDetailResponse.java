package com.sparta.server.threeserving.menu.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sparta.server.threeserving.menu.entity.*;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class MenuDetailResponse {

    // 메뉴 정보
    private UUID id;
    private String name;
    private int price;
    private String description;
    @JsonProperty("isDescriptionAiGenerated")
    private Boolean isDescriptionAiGenerated;
    private MenuStatus status;
    private int displayOrder;
    // private String imageUrl;

    // 메뉴 카테고리 정보
    private UUID categoryId;
    private String categoryName;

    // 옵션 그룹 리스트
    private List<OptionGroupDetail> optionGroups;

    public static MenuDetailResponse from(Menu menu) {
        return MenuDetailResponse.builder()
                .id(menu.getId())
                .name(menu.getName())
                .price(menu.getPrice())
                .description(menu.getDescription())
                .isDescriptionAiGenerated(menu.isDescriptionAiGenerated())
                .status(menu.getStatus())
                .displayOrder(menu.getDisplayOrder())
                .categoryId(menu.getMenuCategory().getId())
                .categoryName(menu.getMenuCategory().getName())
                // 중간 매핑 테이블(MenuOptionGroup)을 거쳐 실제 OptionGroup 데이터만 추출
                .optionGroups(menu.getMenuOptionGroups().stream()
                        .map(mog -> OptionGroupDetail.from(mog.getOptionGroup()))
                        .toList())
                .build();
    }

    @Getter
    @Builder
    public static class OptionGroupDetail {
        private UUID id;
        private String name;
        private int minSelect;
        private int maxSelect;
        private List<OptionItemDetail> optionItems;

        public static OptionGroupDetail from(OptionGroup optionGroup) {
            return OptionGroupDetail.builder()
                    .id(optionGroup.getId())
                    .name(optionGroup.getName())
                    .minSelect(optionGroup.getMinSelect())
                    .maxSelect(optionGroup.getMaxSelect())
                    .optionItems(optionGroup.getOptionItemList().stream()
                            .map(OptionItemDetail::from)
                            .toList())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class OptionItemDetail {
        private UUID id;
        private String name;
        private int price;
        private OptionItemStatus status;
        private int displayOrder;

        public static OptionItemDetail from(OptionItem optionItem) {
            return OptionItemDetail.builder()
                    .id(optionItem.getId())
                    .name(optionItem.getName())
                    .price(optionItem.getPrice())
                    .status(optionItem.getStatus())
                    .displayOrder(optionItem.getDisplayOrder())
                    .build();
        }
    }
}
