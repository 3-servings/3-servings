package com.sparta.server.threeserving.menu.dto.request;

import com.sparta.server.threeserving.image.dto.request.ImageRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuCreateRequest {

    @NotNull(message = "메뉴 카테고리 ID는 필수 입력입니다.")
    private UUID menuCategoryId;

    @NotBlank(message = "메뉴 이름은 필수 입력입니다.")
    @Size(max = 100, message = "메뉴 이름은 100자를 초과할 수 없습니다.")
    private String name;

    @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
    private int price;

    private String description;

    private boolean isDescriptionAiGenerated = false;

    private ImageRequest image;

}
