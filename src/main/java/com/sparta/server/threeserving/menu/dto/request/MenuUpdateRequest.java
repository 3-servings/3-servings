package com.sparta.server.threeserving.menu.dto.request;

import com.sparta.server.threeserving.menu.entity.MenuStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuUpdateRequest {

    @NotNull(message = "메뉴 카테고리 ID는 필수입니다.")
    private UUID menuCategoryId;

    @NotBlank(message = "메뉴 이름은 필수입니다.")
    private String name;

    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private int price;

    private String description;

    @NotNull(message = "메뉴 설명 AI 생성 여부는 필수입니다.")
    private Boolean isDescriptionAiGenerated;

    private MenuStatus status;

}
