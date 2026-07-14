package com.sparta.server.threeserving.ai.dto.request;

import com.sparta.server.threeserving.ai.enums.BaseTone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiMenuDescriptionRequest {

    @NotNull(message = "가게 ID는 필수입니다.")
    private UUID storeId;

    private UUID menuId;

    @NotBlank(message = "메뉴명은 필수 입력값입니다.")
    @Size(max = 50, message = "메뉴명은 최대 50자까지 입력 가능합니다.")
    private String name;

    private Integer price;

    private List<String> ingredients;

    @NotNull(message = "설명 톤앤매너(BaseTone)는 필수 선택값입니다.")
    private BaseTone baseTone;

    private List<String> flavorProfile;

    private List<String> contextTags;

    @Size(max = 200, message = "추가 요청사항은 최대 200자까지 입력 가능합니다.")
    private String additionalRequest;

}
