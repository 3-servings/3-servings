package com.sparta.server.threeserving.menu.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.menu.dto.request.OptionItemStatusUpdateRequest;
import com.sparta.server.threeserving.menu.entity.OptionGroup;
import com.sparta.server.threeserving.menu.entity.OptionItem;
import com.sparta.server.threeserving.menu.entity.OptionItemStatus;
import com.sparta.server.threeserving.menu.repository.OptionItemRepository;
import com.sparta.server.threeserving.store.entity.Store;
import com.sparta.server.threeserving.user.entity.User;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@Tag("menu")
@ExtendWith(MockitoExtension.class)
public class OptionItemServiceTest {

    @InjectMocks
    private OptionItemService optionItemService;

    @Mock
    private OptionItemRepository optionItemRepository;

    @Test
    @DisplayName("실패: 모든 아이템들을 품절시켜 minSelect=1 보다 선택 가능한 아이템이 작은 경우, 예외 발생")
    void updateOptionItemsStatus_Fail_MinSelectViolation() {
        // [Given]
        Long ownerId = 1L;
        User owner = User.builder().id(ownerId).role(UserRoleEnum.OWNER).build();
        Store store = Store.builder().owner(owner).build();

        OptionGroup group = OptionGroup.builder().store(store).name("소스 선택").minSelect(1).maxSelect(2).build();

        UUID ketchupId = UUID.randomUUID();
        UUID mustardId = UUID.randomUUID();
        OptionItem ketchup = OptionItem.builder().optionGroup(group).name("케찹").build();
        OptionItem mustard = OptionItem.builder().optionGroup(group).name("머스타드").build();

        ReflectionTestUtils.setField(ketchup, "id", ketchupId);
        ReflectionTestUtils.setField(ketchup, "status", OptionItemStatus.AVAILABLE);
        ReflectionTestUtils.setField(mustard, "id", mustardId);
        ReflectionTestUtils.setField(mustard, "status", OptionItemStatus.AVAILABLE);

        group.addOptionItem(ketchup);
        group.addOptionItem(mustard);

        // Mocking
        given(optionItemRepository.findByIdIn(anyList())).willReturn(List.of(ketchup, mustard));

        // 클라이언트 요청 : 모두 SOLD_OUT 변경
        OptionItemStatusUpdateRequest.ItemStatusUpdate updateKetchup = new OptionItemStatusUpdateRequest.ItemStatusUpdate(ketchupId, OptionItemStatus.SOLD_OUT);
        OptionItemStatusUpdateRequest.ItemStatusUpdate updateMustard = new OptionItemStatusUpdateRequest.ItemStatusUpdate(mustardId, OptionItemStatus.SOLD_OUT);

        OptionItemStatusUpdateRequest request = new OptionItemStatusUpdateRequest(List.of(updateKetchup, updateMustard));

        // [When & Then]
        CustomException exception = assertThrows(CustomException.class, () -> {
            optionItemService.updateOptionItemsStatus(request, ownerId, UserRoleEnum.OWNER);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.OPTION_MIN_SELECT_VIOLATION);
    }
}
