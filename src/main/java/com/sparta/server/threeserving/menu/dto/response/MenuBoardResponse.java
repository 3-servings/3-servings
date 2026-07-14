package com.sparta.server.threeserving.menu.dto.response;

import com.sparta.server.threeserving.menu.entity.Menu;
import com.sparta.server.threeserving.menu.entity.MenuCategory;
import com.sparta.server.threeserving.menu.entity.MenuStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MenuBoardResponse {

    @Getter
    @Builder
    public static class MenuBoardCategory {
        private UUID menuCategoryId;
        private String menuCategoryName;
        private int displayOrder;
        private List<MenuBoardItem> menus;

        public static MenuBoardCategory from(MenuCategory category, List<Menu> menus, Map<UUID, String> imageUrlMap) {
            return MenuBoardCategory.builder()
                    .menuCategoryId(category.getId())
                    .menuCategoryName(category.getName())
                    .displayOrder(category.getDisplayOrder())
                    .menus(menus.stream()
                            .map(menu -> MenuBoardItem.from(menu, imageUrlMap.getOrDefault(menu.getId(), null)))
                            .toList())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class MenuBoardItem {
        private UUID menuId;
        private String menuName;
        private int price;
        private String description;
        private MenuStatus status;
        private int displayOrder;
        private String imageUrl;

        public static MenuBoardItem from(Menu menu, String imageUrl) {
            return MenuBoardItem.builder()
                    .menuId(menu.getId())
                    .menuName(menu.getName())
                    .price(menu.getPrice())
                    .description(menu.getDescription())
                    .status(menu.getStatus())
                    .displayOrder(menu.getDisplayOrder())
                    .imageUrl(imageUrl)
                    .build();
        }
    }
}
