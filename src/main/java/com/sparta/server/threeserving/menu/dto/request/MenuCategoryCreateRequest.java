package com.sparta.server.threeserving.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuCategoryCreateRequest {

    @NotBlank(message = "카테고리명은 필수 입력 값입니다.")
    private String name;

}
