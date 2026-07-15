package com.sparta.server.threeserving.menu.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sparta.server.threeserving.menu.entity.Menu;
import com.sparta.server.threeserving.menu.enums.MenuStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class MenuResponse {

    private UUID id;
    private UUID storeId;
    private UUID menuCategoryId;
    private String name;
    private int price;
    private String description;
    @JsonProperty("isDescriptionAiGenerated")
    private Boolean isDescriptionAiGenerated;
    private MenuStatus status;
    private int displayOrder;
    private String imageUrl;

    public static MenuResponse from(Menu menu, String imageUrl) {
        return MenuResponse.builder()
                .id(menu.getId())
                .storeId(menu.getStore().getId())
                .menuCategoryId(menu.getMenuCategory().getId())
                .name(menu.getName())
                .price(menu.getPrice())
                .description(menu.getDescription())
                .isDescriptionAiGenerated(menu.isDescriptionAiGenerated())
                .status(menu.getStatus())
                .displayOrder(menu.getDisplayOrder())
                .imageUrl(imageUrl)
                .build();
    }
}
