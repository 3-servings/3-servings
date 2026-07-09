//package com.sparta.server.threeserving.order.service;
//
//import com.sparta.server.threeserving.global.common.response.ApiResponse;
//import com.sparta.server.threeserving.global.common.response.SuccessCode;
//import com.sparta.server.threeserving.order.dto.response.CartResponseDto;
//import com.sparta.server.threeserving.order.entity.Cart;
//import com.sparta.server.threeserving.order.repository.CartRepository;
//import com.sparta.server.threeserving.store.entity.Store;
//import com.sparta.server.threeserving.store.repository.StoreRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
//@ExtendWith(MockitoExtension.class)
//class CartServiceTest {
//
//    @Mock
//    private CartRepository cartRepository;
//
//    @Mock
//    private StoreRepository storeRepository;
//
//    @InjectMocks
//    private CartService cartService;
//
//    @Nested
//    @DisplayName("createOrFindCart")
//    class CreateOrFindCart {
//
//        @Test
//        @DisplayName("해당 가게에 활성 카트가 없으면 새로 생성한다")
//        void createsNewCart_whenNoActiveCartExists() {
//            // given
//            Long userId = 1L;
//            UUID storeId = UUID.randomUUID();
//
//            given(storeRepository.findById(storeId)).willReturn(Optional.of(new Store()));
//            given(cartRepository.findByUserIdAndStoreIdAndDeletedAtIsNull(userId, storeId))
//                    .willReturn(Optional.empty());
//            given(cartRepository.save(any(Cart.class)))
//                    .willAnswer(invocation -> invocation.getArgument(0));
//
//            // when
//            ApiResponse<CartResponseDto> response = cartService.createOrFindCart(userId, storeId);
//
//            // then
//            assertThat(response.getMessage()).isEqualTo(SuccessCode.CREATED.getMessage());
//            assertThat(response.getData().userId()).isEqualTo(userId);
//            assertThat(response.getData().storeId()).isEqualTo(storeId);
//            verify(cartRepository, times(1)).save(any(Cart.class));
//        }
//
//        @Test
//        @DisplayName("해당 가게에 이미 활성 카트가 있으면 기존 카트를 그대로 반환한다")
//        void returnsExistingCart_whenActiveCartAlreadyExists() {
//            // given
//            Long userId = 1L;
//            UUID storeId = UUID.randomUUID();
//            Cart existingCart = new Cart(userId, storeId);
//
//            given(storeRepository.findById(storeId)).willReturn(Optional.of(new Store()));
//            given(cartRepository.findByUserIdAndStoreIdAndDeletedAtIsNull(userId, storeId))
//                    .willReturn(Optional.of(existingCart));
//            given(cartRepository.save(any(Cart.class)))
//                    .willAnswer(invocation -> invocation.getArgument(0));
//
//            // when
//            ApiResponse<CartResponseDto> response = cartService.createOrFindCart(userId, storeId);
//
//            // then
//            assertThat(response.getMessage()).isEqualTo(SuccessCode.SUCCESS.getMessage());
//            assertThat(response.getData().userId()).isEqualTo(existingCart.getUserId());
//            assertThat(response.getData().storeId()).isEqualTo(existingCart.getStoreId());
//            verify(cartRepository, times(1)).save(existingCart);
//        }
//    }
//}
