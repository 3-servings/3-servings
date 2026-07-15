package com.sparta.server.threeserving.store.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.store.dto.request.RegisterStore;
import com.sparta.server.threeserving.store.dto.request.UpdateStoreRequest;
import com.sparta.server.threeserving.store.entity.Category;
import com.sparta.server.threeserving.store.entity.Region;
import com.sparta.server.threeserving.store.entity.Store;
import com.sparta.server.threeserving.store.repository.CategoryRepository;
import com.sparta.server.threeserving.store.repository.RegionRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@Tag("unit")
@Tag("store")
@ExtendWith(MockitoExtension.class)
public class StoreServiceTest {

    @InjectMocks
    private StoreService storeService;

    @Mock
    private StoreRepository storeRepository;
    @Mock
    private RegionRepository regionRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("성공 : 지역/카테고리가 유효하면 가게가 정상 등록된다.")
    void registerStore_Success(){
        // [Given]
        User owner = User.builder().id(1L).role(UserRoleEnum.OWNER).build();

        UUID regionId = UUID.randomUUID();
        Region region = Region.builder().name("광화문").build();
        ReflectionTestUtils.setField(region, "id", regionId);

        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder().name("한식").build();
        ReflectionTestUtils.setField(category, "id", categoryId);

        RegisterStore request = new RegisterStore();
        request.setName("국밥집");
        request.setPhone("01012345678");
        request.setAddress("서울 종로구");
        request.setRegionId(regionId);
        request.setCategoryIds(List.of(categoryId));
        request.setMinOrderPrice(10000);
        request.setDeliveryFee(3000);
        request.setDelivery_radius_m(3000);

        given(regionRepository.findById(regionId)).willReturn(Optional.of(region));
        given(categoryRepository.findAllById(List.of(categoryId))).willReturn(List.of(category));
        given(storeRepository.save(any(Store.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // [When]
        ApiResponse<?> response = storeService.registerStore(request, owner);

        // [Then]
        assertThat(response.getData()).isNotNull();
    }

    @Test
    @DisplayName("실패: 존재하지 않는 지역이면 REGION_NOT_FOUND 예외 발생")
    void registerStore_Fail_RegionNotFound(){
        // [Given]
        User owner = User.builder().id(1L).role(UserRoleEnum.OWNER).build();
        UUID regionId = UUID.randomUUID();

        RegisterStore request = new RegisterStore();
        request.setRegionId(regionId);
        request.setCategoryIds(List.of(UUID.randomUUID()));

        given(regionRepository.findById(regionId)).willReturn(Optional.empty());

        // [When & Then]
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.registerStore(request, owner));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REGION_NOT_FOUND);
    }

    @Test
    @DisplayName("실패: 요청한 카테고리 중 존재하지 않는 게 있으면 CATEGORY_NOT_FOUND 예외 발생")
    void registerStore_Fail_CategoryNotFound(){
        User owner = User.builder().id(1L).role(UserRoleEnum.OWNER).build();

        UUID regionId = UUID.randomUUID();
        Region region = Region.builder().name("광화문").build();
        ReflectionTestUtils.setField(region, "id", regionId);

        UUID validCategoryId = UUID.randomUUID();
        UUID invalidCategoryId = UUID.randomUUID();
        Category validCategory = Category.builder().name("한식").build();
        ReflectionTestUtils.setField(validCategory, "id", validCategoryId);

        RegisterStore request = new RegisterStore();
        request.setRegionId(regionId);
        request.setCategoryIds(List.of(validCategoryId, invalidCategoryId)); // 2개 요청했는데

        given(regionRepository.findById(regionId)).willReturn(Optional.of(region));
        given(categoryRepository.findAllById(List.of(validCategoryId, invalidCategoryId)))
                .willReturn(List.of(validCategory)); // 1개만 조회됨 -> 개수 불일치

        // [When & Then]
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.registerStore(request, owner));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("실패: 가게 주인이 아닌 사용자가 수정 시도하면 ACCESS_DENIED 예외 발생")
    void updateStore_Fail_AccessDenied(){
        // [Given]
        UUID storeId = UUID.randomUUID();
        User realOwner = User.builder().id(1L).role(UserRoleEnum.OWNER).build();
        User hacker = User.builder().id(999L).role(UserRoleEnum.OWNER).build();

        Store store = Store.builder().owner(realOwner).build();
        ReflectionTestUtils.setField(store, "id", storeId);

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

        UpdateStoreRequest request = new UpdateStoreRequest();

        // [When & Then]
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.updateStore(storeId, request, hacker));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 가게 삭제 시도하면 STORE_NOT_FOUND 예외 발생")
    void deleteStore_Fail_StoreNotFound(){
        // [Given]
        UUID storeId = UUID.randomUUID();
        given(storeRepository.findById(storeId)).willReturn(Optional.empty());

        // [When & Then]
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.deleteStore(storeId, 1L));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STORE_NOT_FOUND);
    }
}
