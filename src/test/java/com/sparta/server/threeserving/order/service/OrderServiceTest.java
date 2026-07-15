package com.sparta.server.threeserving.order.service;

import com.sparta.server.threeserving.auth.UserDetailsImpl;
import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.order.dto.request.CartAddItemRequestDto;
import com.sparta.server.threeserving.order.dto.request.CheckoutRequestDto;
import com.sparta.server.threeserving.order.dto.request.OrderModifyRequestDto;
import com.sparta.server.threeserving.order.dto.response.CheckoutResponseDto;
import com.sparta.server.threeserving.order.dto.response.OrderCancelResponseDto;
import com.sparta.server.threeserving.order.dto.response.OrderDetailResponseDto;
import com.sparta.server.threeserving.order.dto.response.OrderModifyResponseDto;
import com.sparta.server.threeserving.order.entity.OrderItem;
import com.sparta.server.threeserving.order.entity.OrderItemOption;
import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order.entity.Orders;
import com.sparta.server.threeserving.order.repository.OrderItemOptionRepository;
import com.sparta.server.threeserving.order.repository.OrderItemRepository;
import com.sparta.server.threeserving.order.repository.OrderRepository;
import com.sparta.server.threeserving.order_management.service.OrderManagementService;
import com.sparta.server.threeserving.store.repository.StoreRepository;
import com.sparta.server.threeserving.user.entity.User;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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

/*
@Tag("OrderService")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
// 로컬에서 cart_service-test-data.sql을 사용한 테스트에서 제대로 동작함.
@ActiveProfiles("ci")
@Transactional
@Sql(scripts = "classpath:sql/cart-service-test-data.sql")
public class OrderServiceTest {
    @Autowired
    private OrderService orderService;
    @Autowired
    private CartService cartService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderItemOptionRepository orderItemOptionRepository;

    @Autowired
    private OrderManagementService orderManagementService;

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


    @BeforeEach
    void authenticateAsTestUser() {
        User fakeUser = User.builder().id(CUSTOMER_ID).role(UserRoleEnum.CUSTOMER).build();
        UserDetailsImpl principal = new UserDetailsImpl(fakeUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("체크아웃 & 검색")
    class CheckoutAndSearch {

        UUID orderId;

        @BeforeEach
        void setUp() {
            // given (공통) - 모든 테스트가 공유할 주문을 하나 미리 체크아웃까지 해둠
            UUID cartId = cartService.createOrFindCart(CUSTOMER_ID, STORE_ID).getData().id();
            cartService.addMenuToCart(CUSTOMER_ID, cartId,
                    new CartAddItemRequestDto(MENU_ID, 2, List.of(OPTION_ITEM_EXTRA_ID))); // (5000+1000)*2 = 12000원
            cartService.addMenuToCart(CUSTOMER_ID, cartId,
                    new CartAddItemRequestDto(NO_OPTION_MENU_ID, 1, List.of())); // 3000원

            CheckoutRequestDto checkoutRequestDto = new CheckoutRequestDto("서울시 종로구 세종대로 172", "문 앞에 놔주세요");
            CheckoutResponseDto checkoutResponse = cartService.checkout(CUSTOMER_ID, cartId, checkoutRequestDto);
            orderId = checkoutResponse.id();
        }

        @Test
        @DisplayName("체크아웃 후에 확인")
        void getOrderDetail() {
            // when
            OrderDetailResponseDto orderResponse = orderService.getOrderDetail(CUSTOMER_ID, UserRoleEnum.CUSTOMER, orderId);

            // then
            assertThat(orderResponse.orderStatus()).isEqualTo(OrderStatusEnum.PENDING);
            assertThat(orderResponse.totalPrice()).isEqualTo(15000);
        }

        @Test
        @DisplayName("다른 사람 주문 확인")
        void getOrderDetailNotOwner() {
            // when & then - OWNER_ID는 이 주문을 한 손님이 아님
            CustomException exception = assertThrows(CustomException.class,
                    () -> orderService.getOrderDetail(OWNER_ID, UserRoleEnum.CUSTOMER, orderId));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_ORDER_OWNER);
        }

        @Test
        @DisplayName("OWNER가 주문 확인")
        void getOrderDetailAsOwner() {
            // when - 이 주문은 STORE_ID(=OWNER_ID 소유 가게) 앞으로 들어온 주문
            OrderDetailResponseDto orderResponse = orderService.getOrderDetail(OWNER_ID, UserRoleEnum.OWNER, orderId);

            // then
            assertThat(orderResponse.id()).isEqualTo(orderId);
            assertThat(orderResponse.storeId()).isEqualTo(STORE_ID);
        }

        @Test
        @DisplayName("주문 Accepted되었는데 취소")
        void cancelAcceptedOrderFails() {
            // given - 이미 ACCEPTED 상태로 전이시켜둠
            Orders order = orderRepository.findById(orderId).orElseThrow();
            order.changeStatus(OrderStatusEnum.ACCEPTED);
            orderRepository.save(order);

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> orderService.cancelOrder(CUSTOMER_ID, orderId));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_ALREADY_PROCESSED);
        }

        @Test
        @DisplayName("주문 취소 성공")
        void cancelOrderSuccess() {
            // when - PENDING 상태 + 생성 5분 이내이므로 취소 가능
            OrderCancelResponseDto response = orderService.cancelOrder(CUSTOMER_ID, orderId);

            // then
            assertThat(response.orderStatus()).isEqualTo(OrderStatusEnum.CANCELED);
            Orders canceledOrder = orderRepository.findById(orderId).orElseThrow();
            assertThat(canceledOrder.getOrderStatus()).isEqualTo(OrderStatusEnum.CANCELED);
        }

        @Test
        @DisplayName("주문 Accepted되었는데 주문정보수정")
        void modifyAcceptedOrderFails() {
            // given - 이미 ACCEPTED 상태로 전이시켜둠
            Orders order = orderRepository.findById(orderId).orElseThrow();
            order.changeStatus(OrderStatusEnum.ACCEPTED);
            orderRepository.save(order);
            OrderModifyRequestDto requestDto = new OrderModifyRequestDto("서울시 강남구 테헤란로 1", "빠르게 부탁드려요");

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> orderService.modifyOrderInfo(CUSTOMER_ID, orderId, requestDto));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_ALREADY_PROCESSED);
        }

        @Test
        @DisplayName("주문 정보 수정 성공")
        void modifyOrderInfoSuccess() {
            // given - PENDING 상태이므로 수정 가능
            OrderModifyRequestDto requestDto = new OrderModifyRequestDto("서울시 강남구 테헤란로 1", "빠르게 부탁드려요");

            // when
            OrderModifyResponseDto response = orderService.modifyOrderInfo(CUSTOMER_ID, orderId, requestDto);

            // then
            assertThat(response.deliveryAddress()).isEqualTo("서울시 강남구 테헤란로 1");
            assertThat(response.requestMessage()).isEqualTo("빠르게 부탁드려요");
        }
    }

    @Nested
    @DisplayName("주문 삭제 (Master 전용 정리용)")
    class DeleteOrder {

        UUID orderId;
        List<OrderItem> orderItems;
        List<OrderItemOption> orderItemOptions;

        @BeforeEach
        void setUp() {
            // given - 옵션이 딸린 주문을 하나 체크아웃까지 해둠
            UUID cartId = cartService.createOrFindCart(CUSTOMER_ID, STORE_ID).getData().id();
            cartService.addMenuToCart(CUSTOMER_ID, cartId,
                    new CartAddItemRequestDto(MENU_ID, 1, List.of(OPTION_ITEM_EXTRA_ID)));

            CheckoutRequestDto checkoutRequestDto = new CheckoutRequestDto("서울시 종로구 세종대로 172", null);
            orderId = cartService.checkout(CUSTOMER_ID, cartId, checkoutRequestDto).id();

            Orders order = orderRepository.findById(orderId).orElseThrow();
            orderItems = orderItemRepository.findAllByOrderAndDeletedAtIsNull(order);
            orderItemOptions = orderItemOptionRepository.findAllByOrderItemIn(orderItems);

            // 사전 조건 확인
            assertThat(orderItems).isNotEmpty();
            assertThat(orderItemOptions).isNotEmpty();
        }

        @Test
        @DisplayName("삭제 시 Order/OrderItem은 soft delete, OrderItemOption은 hard delete")
        void deleteOrderSoftAndHardDelete() {
            // when
            orderService.deleteOrder(OWNER_ID, orderId);

            // then - Order: 행은 남아있지만 deletedAt이 채워지고, deletedAt IS NULL 조건으로는 더 이상 안 잡힘
            Orders deletedOrder = orderRepository.findById(orderId).orElseThrow();
            assertThat(deletedOrder.getDeletedAt()).isNotNull();
            assertThat(orderRepository.findByIdAndDeletedAtIsNull(orderId)).isEmpty();

            // then - OrderItem: 마찬가지로 soft delete (행은 남고 deletedAt만 채워짐)
            for (OrderItem item : orderItems) {
                OrderItem deletedItem = orderItemRepository.findById(item.getId()).orElseThrow();
                assertThat(deletedItem.getDeletedAt()).isNotNull();
            }
            assertThat(orderItemRepository.findAllByOrderAndDeletedAtIsNull(deletedOrder)).isEmpty();

            // then - OrderItemOption: p_order_item_option엔 deleted_at 컬럼이 없어서 soft delete가 불가능
            // -> 행 자체가 물리적으로 사라져야 함 (hard delete)
            List<OrderItemOption> remainingOptions = orderItemOptionRepository.findAllByOrderItemIn(orderItems);
            assertThat(remainingOptions).isEmpty();
            for (OrderItemOption option : orderItemOptions) {
                assertThat(orderItemOptionRepository.findById(option.getId())).isEmpty();
            }
        }
    }

}
*/