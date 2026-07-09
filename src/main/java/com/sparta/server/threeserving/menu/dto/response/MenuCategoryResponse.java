package com.sparta.server.threeserving.menu.dto.response;

import com.sparta.server.threeserving.menu.entity.MenuCategory;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class MenuCategoryResponse {

    private final UUID id;
    private final String name;
    private final int displayOrder;

    public static MenuCategoryResponse from(MenuCategory menuCategory) {
        return MenuCategoryResponse.builder()
                .id(menuCategory.getId())
                .name(menuCategory.getName())
                .displayOrder(menuCategory.getDisplayOrder())
                .build();
    }
}
