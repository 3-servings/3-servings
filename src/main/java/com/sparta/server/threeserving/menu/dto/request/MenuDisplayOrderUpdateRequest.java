package com.sparta.server.threeserving.menu.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuDisplayOrderUpdateRequest {

    @NotNull(message = "메뉴 카테고리 ID는 필수입니다.")
    private UUID menuCategoryId;

    @NotEmpty(message = "정렬된 메뉴 ID 목록은 필수입니다.")
    private List<UUID> menuIds;

}
