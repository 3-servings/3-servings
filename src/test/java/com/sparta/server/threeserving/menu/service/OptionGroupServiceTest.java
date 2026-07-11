package com.sparta.server.threeserving.menu.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.menu.dto.request.OptionGroupCreateRequest;
import com.sparta.server.threeserving.menu.dto.request.OptionGroupUpdateRequest;
import com.sparta.server.threeserving.menu.dto.response.OptionGroupResponse;
import com.sparta.server.threeserving.menu.entity.OptionGroup;
import com.sparta.server.threeserving.menu.entity.OptionItem;
import com.sparta.server.threeserving.menu.repository.OptionGroupRepository;
import com.sparta.server.threeserving.store.entity.Store;
import com.sparta.server.threeserving.store.repository.StoreRepository;
import com.sparta.server.threeserving.user.entity.User;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@Tag("unit")
@Tag("menu")
@ExtendWith(MockitoExtension.class)
public class OptionGroupServiceTest {

    @InjectMocks
    private OptionGroupService optionGroupService;

    @Mock
    private OptionGroupRepository optionGroupRepository;
    @Mock
    private StoreRepository storeRepository;

    @Test
    @DisplayName("성공: Delta Update - 기존 아이템 수정, 누락된 아이템 삭제, 신규 아이템 추가가 완벽히 동작한다.")
    void updateOptionGroup_DeltaUpdate_Success() {
        // [Given]
        // 데이터 준비
        UUID groupId = UUID.randomUUID();
        Long ownerId = 1L;
        User owner = User.builder().id(ownerId).role(UserRoleEnum.OWNER).build();
        Store store = Store.builder().owner(owner).build();
        ReflectionTestUtils.setField(store, "id", UUID.randomUUID());

        OptionGroup existingGroup = OptionGroup.builder()
                .store(store).name("소스 선택").minSelect(0).maxSelect(2).build();

        UUID ketchupId = UUID.randomUUID();
        UUID mustardId = UUID.randomUUID();
        OptionItem ketchup = OptionItem.builder().optionGroup(existingGroup).name("케찹").price(0).build();
        OptionItem mustard = OptionItem.builder().optionGroup(existingGroup).name("머스타드").price(0).build();
        ReflectionTestUtils.setField(ketchup, "id", ketchupId);
        ReflectionTestUtils.setField(mustard, "id", mustardId);

        existingGroup.addOptionItem(ketchup);
        existingGroup.addOptionItem(mustard);

        // Mocking
        given(optionGroupRepository.findById(groupId)).willReturn(Optional.of(existingGroup));
        given(optionGroupRepository.existsByStoreIdAndNameAndIdNot(store.getId(), "소스 선택 (수정)", groupId))
                .willReturn(false);

        // 수정 요청: 케찹 가격 500원 인상, 머스타드 누락 (삭제), 핫소스 신규 추가
        OptionGroupUpdateRequest.OptionItemRequest updateKetchup = new OptionGroupUpdateRequest.OptionItemRequest(ketchupId, "케찹", 500);
        OptionGroupUpdateRequest.OptionItemRequest addHotSauce = new OptionGroupUpdateRequest.OptionItemRequest(null, "핫소스", 0);

        OptionGroupUpdateRequest request = new OptionGroupUpdateRequest("소스 선택 (수정)", 0, 2, List.of(updateKetchup, addHotSauce));

        // [When]
        OptionGroupResponse response = optionGroupService.updateOptionGroup(groupId, request, ownerId, UserRoleEnum.OWNER);

        // [Then]
        assertThat(response.getName()).isEqualTo("소스 선택 (수정)");
        assertThat(response.getOptionItems()).hasSize(2);

        // 가격 확인
        OptionGroupResponse.OptionItemResponse ketchupRes = response.getOptionItems().stream()
                .filter(item -> item.getName().equals("케찹"))
                .findFirst().orElseThrow();
        assertThat(ketchupRes.getPrice()).isEqualTo(500);
        assertThat(ketchupRes.getId()).isEqualTo(ketchupId); // 기존 ID 유지 확인

        // 신규 추가 확인
        boolean hasHotSauce = response.getOptionItems().stream()
                .anyMatch(item -> item.getName().equals("핫소스") && item.getId() == null); // DTO 변환 시 영속화 전이라 ID가 null
        assertThat(hasHotSauce).isTrue();

        // 삭제 확인
        boolean hasMustard = response.getOptionItems().stream()
                .anyMatch(item -> item.getName().equals("머스타드"));
        assertThat(hasMustard).isFalse();
    }

    @Test
    @DisplayName("실패: minSelect가 maxSelect보다 크면, INVALID_OPTION_SELECTION 예외 발생")
    void createOptionGroup_Fail_InvalidSelection() {
        // [Given]
        UUID storeId = UUID.randomUUID();

        OptionGroupCreateRequest.OptionItemRequest itemReq = new OptionGroupCreateRequest.OptionItemRequest("케찹", 0);
        OptionGroupCreateRequest request = new OptionGroupCreateRequest("에러 유발", 2, 1, List.of(itemReq));

        // [When & Then]
        CustomException exception = assertThrows(CustomException.class, () -> {
            optionGroupService.createOptionGroup(storeId, request, 1L, UserRoleEnum.OWNER);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_OPTION_SELECTION);
    }

    @Test
    @DisplayName("실패: 가게 주인이 아닌 다른 사용자가 생성 시도시, ACCESS_DENIED 예외 발생")
    void createOptionGroup_Fail_AccessDenied() {
        // [Given]
        UUID storeId = UUID.randomUUID();
        Long ownerId = 1L;
        Long hackerId = 999L;

        User owner = User.builder().id(ownerId).role(UserRoleEnum.OWNER).build();
        Store store = Store.builder().id(storeId).owner(owner).build();

        OptionGroupCreateRequest.OptionItemRequest itemReq =
                new OptionGroupCreateRequest.OptionItemRequest("케찹", 0);
        OptionGroupCreateRequest request =
                new OptionGroupCreateRequest("소스 선택", 0, 1, List.of(itemReq));

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

        // [When & Then]
        CustomException exception = assertThrows(CustomException.class, () -> {
            optionGroupService.createOptionGroup(storeId, request, hackerId, UserRoleEnum.OWNER);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED);
    }
}
