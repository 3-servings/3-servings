package com.sparta.server.threeserving.order.service;

import com.sparta.server.threeserving.auth.UserDetailsImpl;
import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.menu.entity.Menu;
import com.sparta.server.threeserving.menu.repository.MenuOptionGroupRepository;
import com.sparta.server.threeserving.menu.repository.MenuRepository;
import com.sparta.server.threeserving.menu.repository.OptionGroupRepository;
import com.sparta.server.threeserving.menu.repository.OptionItemRepository;
import com.sparta.server.threeserving.order.dto.request.CartAddItemRequestDto;
import com.sparta.server.threeserving.order.dto.request.CartUpdateItemAmountRequestDto;
import com.sparta.server.threeserving.order.dto.request.CheckoutRequestDto;
import com.sparta.server.threeserving.order.dto.response.*;
import com.sparta.server.threeserving.order.entity.*;
import com.sparta.server.threeserving.order.repository.*;
import com.sparta.server.threeserving.order_management.service.OrderManagementService;
import com.sparta.server.threeserving.store.entity.Store;
import com.sparta.server.threeserving.store.repository.StoreRepository;
import com.sparta.server.threeserving.user.entity.User;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("CartService")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("local")
@Transactional
@Sql(scripts = "classpath:sql/cart-service-test-data.sql")
public class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private CartItemOptionRepository cartItemOptionRepository;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private StoreRepository storeRepository;

    // sql/cart-service-test-data.sql 에서 심어둔 고정 id들
    private static final Long OWNER_ID = 900001L;
    private static final Long CUSTOMER_ID = 900002L;
    private static final UUID REGION_ID = UUID.fromString("a1000000-0000-0000-0000-000000000001");
    private static final UUID STORE_ID = UUID.fromString("a1000000-0000-0000-0000-000000000002");
    private static final UUID MENU_CATEGORY_ID = UUID.fromString("a1000000-0000-0000-0000-000000000003");
    private static final UUID MENU_ID = UUID.fromString("a1000000-0000-0000-0000-000000000004"); // 자장면, 5000원, 옵션그룹 필수 1개
    private static final UUID OPTION_GROUP_ID = UUID.fromString("a1000000-0000-0000-0000-000000000005");
    private static final UUID OPTION_ITEM_NORMAL_ID = UUID.fromString("a1000000-0000-0000-0000-000000000006"); // 보통, 0원
    private static final UUID OPTION_ITEM_EXTRA_ID = UUID.fromString("a1000000-0000-0000-0000-000000000007");  // 곱빼기, 1000원
    private static final UUID NO_OPTION_MENU_ID = UUID.fromString("a1000000-0000-0000-0000-000000000009");     // 군만두, 3000원, 옵션그룹 없음
    private static final UUID SOLD_OUT_MENU_ID = UUID.fromString("a1000000-0000-0000-0000-00000000000a");      // 품절메뉴, STORE_ID 소속, SOLD_OUT
    private static final UUID OTHER_STORE_MENU_ID = UUID.fromString("a2000000-0000-0000-0000-000000000003");   // 다른가게메뉴, 다른 가게 소속

    // JPA Auditing(@CreatedBy)이 SecurityContext에서 현재 유저를 읽는데, 서비스 메서드를 직접 호출하는
    // 이 테스트는 JwtAuthorizationFilter를 안 거쳐서 SecurityContext가 비어있음 -> created_by NOT NULL 위반 발생.
    // 비즈니스 로직(소유권 체크 등)은 각 서비스 메서드에 넘기는 userId 파라미터로 결정되므로,
    // 여기서는 created_by를 채우기 위한 용도로만 고정된 가짜 인증 하나를 심어둠.
    @BeforeEach
    void authenticateAsTestUser() {
        User fakeUser = User.builder().id(CUSTOMER_ID).role(UserRoleEnum.CUSTOMER).build();
        UserDetailsImpl principal = new UserDetailsImpl(fakeUser);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("카트 생성 - 처음엔 새거 (CREATED), 두 번째는 기존 카트(SUCCESS)")
    void createCart(){
        // when - 처음 호출: 활성 카트가 없으므로 새로 생성됨
        ApiResponse<CartResponseDto> response = cartService.createOrFindCart(CUSTOMER_ID, STORE_ID);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getData().storeId()).isEqualTo(STORE_ID);
        assertThat(response.getMessage()).isEqualTo(SuccessCode.CREATED.getMessage());

        // when - 두 번째 호출: 방금 만든 카트를 그대로 반환해야 함 (find-or-create)
        ApiResponse<CartResponseDto> response1 = cartService.createOrFindCart(CUSTOMER_ID, STORE_ID);

        // then
        assertThat(response1).isNotNull();
        assertThat(response1.getData().id()).isEqualTo(response.getData().id());
        assertThat(response1.getData().storeId()).isEqualTo(STORE_ID);
        assertThat(response1.getMessage()).isEqualTo(SuccessCode.SUCCESS.getMessage());

        // 두 번 호출해도 카트가 실제로 1개만 존재하는지 DB로 직접 확인
        List<Cart> allCarts = cartRepository.findAllByUserIdAndDeletedAtIsNull(CUSTOMER_ID);
        assertThat(allCarts).hasSize(1);
    }

    @Nested
    @DisplayName("카트 아이템 담기 / 수량 변경 / 삭제")
    class CartItemLifecycle {

        private UUID cartId;

        @BeforeEach
        void setUp() {
            // given (공통) - 세 테스트 전부 '카트가 이미 있음'을 전제로 하므로 여기서 미리 만들어둠
            cartId = cartService.createOrFindCart(CUSTOMER_ID, STORE_ID).getData().id();
        }

        @Test
        @DisplayName("옵션 없는 메뉴 - 총액: 메뉴 가격 * 수량")
        void addMenuWithoutOptions() {
            // given
            CartAddItemRequestDto requestDto = new CartAddItemRequestDto(NO_OPTION_MENU_ID, 2, List.of());

            // when
            CartAddItemResponseDto response = cartService.addMenuToCart(CUSTOMER_ID, cartId, requestDto);

            // then
            assertThat(response.menuId()).isEqualTo(NO_OPTION_MENU_ID);
            assertThat(response.quantity()).isEqualTo(2);

            CartDetailResponseDto detail = cartService.getCartDetail(CUSTOMER_ID, cartId);
            assertThat(detail.estimatedTotalPrice()).isEqualTo(3000 * 2); // 군만두 3000원 * 2개, 옵션 없음
        }

        @Test
        @DisplayName("옵션 가격 합산 총액")
        void addMenuWithOptions() {
            // given
            CartAddItemRequestDto requestDto = new CartAddItemRequestDto(MENU_ID, 1, List.of(OPTION_ITEM_EXTRA_ID));

            // when
            CartAddItemResponseDto response = cartService.addMenuToCart(CUSTOMER_ID, cartId, requestDto);

            // then
            assertThat(response.menuId()).isEqualTo(MENU_ID);
            assertThat(response.quantity()).isEqualTo(1);

            CartDetailResponseDto detail = cartService.getCartDetail(CUSTOMER_ID, cartId);
            assertThat(detail.estimatedTotalPrice()).isEqualTo(5000 + 1000); // 자장면 5000원 + 곱빼기 1000원

            CartItemDetailResponseDto item = detail.items().getFirst();
            assertThat(item.menuName()).isEqualTo("자장면");
            assertThat(item.options()).hasSize(1);
            assertThat(item.options().getFirst().optionName()).isEqualTo("곱빼기");
        }

        @Test
        @DisplayName("옵션 그룹 최대 선택 개수 초과")
        void addMenuWithTooManyOptions() {
            // given - '곱빼기 여부' 그룹은 max_select=1인데 옵션 2개(보통+곱빼기)를 같이 선택
            CartAddItemRequestDto requestDto = new CartAddItemRequestDto(
                    MENU_ID, 1, List.of(OPTION_ITEM_NORMAL_ID, OPTION_ITEM_EXTRA_ID)
            );

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> cartService.addMenuToCart(CUSTOMER_ID, cartId, requestDto));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CART_OPTION_GROUP_SELECTION_INVALID);
        }

        @Test
        @DisplayName("품절된 메뉴")
        void addSoldOutMenuFails() {
            // given
            CartAddItemRequestDto requestDto = new CartAddItemRequestDto(SOLD_OUT_MENU_ID, 1, List.of());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> cartService.addMenuToCart(CUSTOMER_ID, cartId, requestDto));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ADD_UNAVAILABLE_MENU_TO_CART);
        }

        @Test
        @DisplayName("다른 가게의 메뉴")
        void addMenuFromOtherStoreFails() {
            // given - cartId는 STORE_ID 소속 카트인데, 다른 가게 메뉴를 담으려 함
            CartAddItemRequestDto requestDto = new CartAddItemRequestDto(OTHER_STORE_MENU_ID, 1, List.of());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> cartService.addMenuToCart(CUSTOMER_ID, cartId, requestDto));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ADD_MENU_OF_OTHER_STORE_TO_CART);
        }

        @Test
        @DisplayName("항목 수량 변경")
        void updateCartItemAmountSuccess() {
            // given - 먼저 메뉴를 담아둠
            CartAddItemResponseDto added = cartService.addMenuToCart(
                    CUSTOMER_ID, cartId, new CartAddItemRequestDto(NO_OPTION_MENU_ID, 1, List.of()));
            CartUpdateItemAmountRequestDto requestDto = new CartUpdateItemAmountRequestDto(5);

            // when
            CartUpdateItemAmountResponseDto response = cartService.updateCartItemAmount(
                    CUSTOMER_ID, cartId, added.id(), requestDto);

            // then
            assertThat(response.quantity()).isEqualTo(5);
            CartItem updated = cartItemRepository.findById(added.id()).orElseThrow();
            assertThat(updated.getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("본인 소유가 아닌 카트 삭제 - 예외 발생")
        void deleteCartItemNotOwner() {
            // given
            CartAddItemResponseDto added = cartService.addMenuToCart(
                    CUSTOMER_ID, cartId, new CartAddItemRequestDto(NO_OPTION_MENU_ID, 1, List.of()));

            // when & then - OWNER_ID
            CustomException exception = assertThrows(CustomException.class,
                    () -> cartService.deleteCartItem(OWNER_ID, cartId, added.id()));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_CART_OWNER);
        }

        @Test
        @DisplayName("삭제 후 조회")
        void deleteCartItemSuccess() {
            // given
            CartAddItemResponseDto added = cartService.addMenuToCart(
                    CUSTOMER_ID, cartId, new CartAddItemRequestDto(NO_OPTION_MENU_ID, 1, List.of()));

            // when
            ApiResponse<Void> response = cartService.deleteCartItem(CUSTOMER_ID, cartId, added.id());

            // then
            assertThat(response.getMessage()).isEqualTo(SuccessCode.DELETED.getMessage());
            assertThat(cartItemRepository.findByIdAndCart_IdAndDeletedAtIsNull(added.id(), cartId)).isEmpty();

            CartDetailResponseDto detail = cartService.getCartDetail(CUSTOMER_ID, cartId);
            assertThat(detail.items()).isEmpty();
        }
    }

    @Nested
    @DisplayName("체크아웃")
    class Checkout {

        private UUID cartId;

        @BeforeEach
        void setUp() {
            // given (공통) - 체크아웃 대상 카트만 미리 만들어둠. 아이템은 각 테스트에서 필요한 만큼 담음.
            cartId = cartService.createOrFindCart(CUSTOMER_ID, STORE_ID).getData().id();
        }

        @Test
        @DisplayName("체크아웃 후 주문이 생성 확인 및 카트 soft delete 확인")
        void checkoutSuccess() {
            // given - 자장면(옵션: 곱빼기) 2개를 담아둠
            cartService.addMenuToCart(CUSTOMER_ID, cartId,
                    new CartAddItemRequestDto(MENU_ID, 2, List.of(OPTION_ITEM_EXTRA_ID)));
            CheckoutRequestDto requestDto = new CheckoutRequestDto("서울시 종로구 세종대로 172", "문 앞에 놔주세요");

            // when
            CheckoutResponseDto response = cartService.checkout(CUSTOMER_ID, cartId, requestDto);

            // then
            assertThat(response.orderStatus()).isEqualTo(OrderStatusEnum.PENDING);
            assertThat(response.deliveryAddress()).isEqualTo("서울시 종로구 세종대로 172");

            Orders savedOrder = orderRepository.findById(response.id()).orElseThrow();
            assertThat(savedOrder.getTotalPrice()).isEqualTo((5000 + 1000) * 2); // (자장면 + 곱빼기) * 2개

            List<OrderItem> orderItems = orderItemRepository.findAllByOrderAndDeletedAtIsNull(savedOrder);
            assertThat(orderItems).hasSize(1);
            assertThat(orderItems.getFirst().getMenuName()).isEqualTo("자장면");
            assertThat(orderItems.getFirst().getQuantity()).isEqualTo(2);

            // 카트는 soft delete 되어 더 이상 활성 카트로 조회되면 안 됨
            assertThat(cartRepository.findByIdAndDeletedAtIsNull(cartId)).isEmpty();
        }

        @Test
        @DisplayName("빈 카트로 체크아웃")
        void checkoutEmptyCart() {
            // given - cartId에 아무 것도 안 담음 (setUp에서 카트만 생성됨)
            CheckoutRequestDto requestDto = new CheckoutRequestDto("서울시 종로구 세종대로 172", null);

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> cartService.checkout(CUSTOMER_ID, cartId, requestDto));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_ITEMS_IS_EMPTY);
        }

        @Test
        @DisplayName("담은 뒤 가게/메뉴 정보가 바뀌어도 카트 조회와 체크아웃 스냅샷은 최신 값 반영")
        void checkoutSnapshotsLatestStoreAndMenuInfo() {
            // given - 원래 이름/가격(자장면, 5000원)으로 담아둠
            // 자장면은 '곱빼기 여부' 옵션 그룹이 필수(min=1)라 옵션 없이는 못 담음 -> 0원짜리 '보통' 옵션으로 채움
            cartService.addMenuToCart(CUSTOMER_ID, cartId,
                    new CartAddItemRequestDto(MENU_ID, 1, List.of(OPTION_ITEM_NORMAL_ID)));
            cartService.addMenuToCart(CUSTOMER_ID, cartId,
                    new CartAddItemRequestDto(MENU_ID, 1, List.of(OPTION_ITEM_NORMAL_ID)));

            // Cart는 Menu/Store를 FK로 안 물고 id로만 참조하므로, 담긴 뒤 원본이 바뀌어도
            // 카트 자체엔 아무 스냅샷도 없음 -> 조회할 때마다 항상 "현재" 값을 봐야 함
            Store store = storeRepository.findById(STORE_ID).orElseThrow();
            store.update("신장개업 가게", store.getPhone(), store.getAddress(), store.getDetailAddress(),
                    store.getMinOrderPrice(), store.getDeliveryFee(), store.getDeliveryRadiusM());
            storeRepository.save(store);

            Menu menu = menuRepository.findById(MENU_ID).orElseThrow();
            menu.update(menu.getMenuCategory(), "자장면(리뉴얼)", 6000, menu.getDescription(),
                    menu.isDescriptionAiGenerated(), menu.getStatus());
            menuRepository.save(menu);

            // when & then 1 - 체크아웃 전 카트 목록/상세 조회는 바뀐 가게명/메뉴명/가격을 그대로 반영해야 함
            List<CartListResponseDto> cartList = cartService.getCartList(CUSTOMER_ID);
            assertThat(cartList).anySatisfy(c -> assertThat(c.storeName()).isEqualTo("신장개업 가게"));

            CartDetailResponseDto detailBeforeCheckout = cartService.getCartDetail(CUSTOMER_ID, cartId);
            assertThat(detailBeforeCheckout.estimatedTotalPrice()).isEqualTo(6000 * 2);
            assertThat(detailBeforeCheckout.items().getFirst().menuName()).isEqualTo("자장면(리뉴얼)");

            // when - 체크아웃 시점에도 담을 때(5000원)가 아니라 지금(6000원) 기준으로 스냅샷돼야 함
            CheckoutResponseDto response = cartService.checkout(
                    CUSTOMER_ID, cartId, new CheckoutRequestDto("서울시 종로구 세종대로 172", null));

            // then 2 - Order/OrderItem 스냅샷도 최신 값 기준
            Orders savedOrder = orderRepository.findById(response.id()).orElseThrow();
            assertThat(savedOrder.getTotalPrice()).isEqualTo(6000 * 2);

            List<OrderItem> orderItems = orderItemRepository.findAllByOrderAndDeletedAtIsNull(savedOrder);
            assertThat(orderItems).hasSize(2);
            assertThat(orderItems).allSatisfy(item -> {
                assertThat(item.getMenuName()).isEqualTo("자장면(리뉴얼)"); // 담을 때의 "자장면"이 아님
                assertThat(item.getPrice()).isEqualTo(6000); // 담을 때의 5000원이 아님
            });
        }
    }
}
