package com.sparta.server.threeserving.menu.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuCategoryUpdateRequest {

    @NotBlank(message = "카테고리명은 필수 입력 값입니다.")
    private String name;

    @NotNull(message = "노출 순서는 필수 입력 값입니다.")
    @PositiveOrZero(message = "노출 순서는 0 이상이어야 합니다.")
    private Integer displayOrder;

}
