package com.sparta.server.threeserving.menu.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuOptionGroupAssignRequest {

    @NotNull(message = "옵션 그룹 ID 목록은 필수입니다. 비우려면 빈 배열을 전송하세요")
    private List<UUID> optionGroupIds;

}
