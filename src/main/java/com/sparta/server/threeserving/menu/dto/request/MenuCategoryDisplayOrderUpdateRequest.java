package com.sparta.server.threeserving.menu.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuCategoryDisplayOrderUpdateRequest {

    @NotNull(message = "메뉴 카테고리 ID 리스트는 필수 입력입니다.")
    private List<UUID> menuCategoryIds;

}
