package com.sparta.server.threeserving.menu.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OptionGroupUpdateRequest {

    @NotBlank(message = "옵션 그룹 이름은 필수입니다.")
    private String name;

    @NotNull(message = "최소 선택 개수는 필수입니다.")
    @Min(value = 0, message = "최소 선택 개수는 0 이상이어야 합니다.")
    private Integer minSelect;

    @NotNull(message = "최대 선택 개수는 필수입니다.")
    @Min(value = 1, message = "최대 선택 개수는 1 이상이어야 합니다.")
    private Integer maxSelect;

    @NotEmpty(message = "옵션 아이템은 최소 하나 이상이어야 합니다.")
    @Valid
    private List<OptionItemRequest> optionItems;

    @Getter
    @NoArgsConstructor  // Jackson 메시지 컨버터용 - 파라미터가 없는 기본 생성자 생성
    @AllArgsConstructor // 테스트 코드 빌드용 - 모든 필드를 인자로 받는 생성자 생성
    public static class OptionItemRequest {

        private UUID id; // 새로 추가되는 아이템은 id가 null

        @NotBlank(message = "옵션 항목 이름은 필수입니다.")
        private String name;

        @NotNull(message = "가격은 필수입니다.")
        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        private Integer price;
    }
}
