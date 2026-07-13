package com.sparta.server.threeserving.menu.dto.request;

import com.sparta.server.threeserving.menu.entity.MenuStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuStatusUpdateRequest {

    @NotEmpty(message = "상태를 변경할 메뉴 ID 목록은 필수입니다.")
    private List<UUID> menuIds;

    @NotNull(message = "변경할 상태 값은 필수입니다.")
    private MenuStatus status;

}
