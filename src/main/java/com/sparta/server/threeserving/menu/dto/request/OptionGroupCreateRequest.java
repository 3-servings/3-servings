package com.sparta.server.threeserving.menu.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class OptionGroupCreateRequest {

    @NotBlank(message = "옵션 그룹 이름은 필수입니다.")
    private String name;

    @NotNull(message = "최소 선택 개수는 필수입니다.")
    private int minSelect;

    @NotNull(message = "최대 선택 개수는 필수입니다.")
    private int maxSelect;

    @NotEmpty(message = "옵션 아이템은 최소 하나 이상이어야 합니다.")
    @Valid
    private List<OptionItemRequest> optionItems;

    @Getter
    public static class OptionItemRequest {
        @NotBlank(message = "옵션 항목 이름은 필수입니다.")
        private String name;

        @NotNull(message = "가격은 필수입니다.")
        private int price;
    }

}
