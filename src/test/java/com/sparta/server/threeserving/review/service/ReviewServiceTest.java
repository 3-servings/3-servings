package com.sparta.server.threeserving.review.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.image.service.ImageService;
import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order.entity.Orders;
import com.sparta.server.threeserving.order.repository.OrderRepository;
import com.sparta.server.threeserving.review.dto.ReviewCreateRequest;
import com.sparta.server.threeserving.review.repository.ReviewCommentRepository;
import com.sparta.server.threeserving.review.repository.ReviewRepository;
import com.sparta.server.threeserving.store.repository.StoreRepository;
import com.sparta.server.threeserving.user.entity.User;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    ReviewRepository reviewRepository;
    @Mock
    StoreRepository storeRepository;
    @Mock
    ReviewCommentRepository reviewCommentRepository;
    @Mock
    OrderRepository orderRepository;
    @Mock
    ImageService imageService;
    @InjectMocks
    ReviewService reviewService;


    private User loginUser(Long id){
        User user = User.create("customer01", "닉네임", "c@test.com", "PW", "01011112222", UserRoleEnum.CUSTOMER);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }


    private Orders order(Long userId, OrderStatusEnum status){
        Orders order = new Orders(userId, UUID.randomUUID(), null, status, 10000, "서울", null);
        ReflectionTestUtils.setField(order, "id", UUID.randomUUID());
        return order;
    }

    @Test
    @DisplayName("작성 실패 - 주문 없음")
    void create_fail_order_not_found(){
        UUID orderId = UUID.randomUUID();
        ReviewCreateRequest req = new ReviewCreateRequest(orderId, 5, "맛있어요", List.of());

        given(orderRepository.findById(orderId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(loginUser(1L), req))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_NOT_FOUND);
    }


    @Test
    @DisplayName("작성실패 - 본인 주문 아님")
    void create_fail_not_order_owner(){
        Orders order = order(999L, OrderStatusEnum.COMPLETED); // 주문자 999
        ReviewCreateRequest req = new ReviewCreateRequest(order.getId(), 5, "맛있어요", List.of());
        given(orderRepository.findById(order.getId())).willReturn(Optional.of(order));
        assertThatThrownBy(() -> reviewService.createReview(loginUser(1L), req))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_ORDER_OWNER_OF_REVIEW);
    }


    @Test
    @DisplayName("작성 실패 - 배송 미완료 주문")
    void create_fail_order_not_completed(){
        Orders order = order(1L, OrderStatusEnum.PENDING); // 컴플리트 아님
        ReviewCreateRequest req = new ReviewCreateRequest(order.getId(), 5, "맛있어요", List.of());

        given(orderRepository.findById(order.getId())).willReturn(Optional.of(order));

        assertThatThrownBy(() -> reviewService.createReview(loginUser(1L), req))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_NOT_COMPLETED);
    }


    @Test
    @DisplayName("작성 실패 - 이미 리뷰 존재")
    void create_fail_already_exists(){
        Orders order = order(1L, OrderStatusEnum.COMPLETED);
        ReviewCreateRequest req = new ReviewCreateRequest(order.getId(), 5, "맛있어요", List.of());
        given(orderRepository.findById(order.getId())).willReturn(Optional.of(order));
        given(reviewRepository.existsByOrder_IdAndDeletedAtIsNull(order.getId())).willReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(loginUser(1L), req))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_ALREADY_EXISTS);
    }









}
