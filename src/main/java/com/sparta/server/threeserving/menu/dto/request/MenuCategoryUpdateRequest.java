package com.sparta.server.threeserving.menu.dto.request;

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

}
