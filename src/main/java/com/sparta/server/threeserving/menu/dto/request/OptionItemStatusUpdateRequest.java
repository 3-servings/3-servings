package com.sparta.server.threeserving.menu.dto.request;

import com.sparta.server.threeserving.menu.entity.OptionItemStatus;
import jakarta.validation.Valid;
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
public class OptionItemStatusUpdateRequest {

    @NotEmpty(message = "상태를 변경할 아이템 목록은 필수입니다.")
    @Valid
    private List<ItemStatusUpdate> itemStatusUpdates;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemStatusUpdate {
        @NotNull(message = "아이템 ID는 필수입니다.")
        private UUID optionItemId;

        @NotNull(message = "변경할 상태값은 필수입니다.")
        private OptionItemStatus status;
    }

}
