package com.sparta.server.threeserving.review.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order.entity.Orders;
import com.sparta.server.threeserving.review.dto.*;
import com.sparta.server.threeserving.review.entity.Review;
import com.sparta.server.threeserving.review.repository.ReviewCommentRepository;
import com.sparta.server.threeserving.review.repository.ReviewRepository;
import com.sparta.server.threeserving.store.entity.Store;
import com.sparta.server.threeserving.store.repository.StoreRepository;
import com.sparta.server.threeserving.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@Slf4j // 로그
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;   // 가게 연관 세팅 + 평점 재계산
    private final ReviewCommentRepository reviewCommentRepository;
    private final OrderRepository orderRepository;


    //작성 COMPLETED 주문 or 본인 주문에만 (feat: 리뷰 작성기능 구현)
    @Transactional
    public ReviewResponse createReview(User loginUser, ReviewCreateRequest request){
        Orders order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserId().equals(loginUser.getId())){
            throw new CustomException(ErrorCode.NOT_ORDER_OWNER);
        }
        if (order.getOrderStatus() != OrderStatusEnum.COMPLETED){
            throw new CustomException(ErrorCode.ORDER_NOT_COMPLETED);
        }
        if (reviewRepository.existsByOrder_IdAndDeletedAtIsNull(order.getId())){
            throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Store store = storeRepository.findById(order.getStoreId())
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        Review review = Review.create(order, loginUser, store, request.star(), request.content());

        reviewRepository.save(review);

        recalculateStoreRating(store);
        return new ReviewResponse(review);
    }

    //상세 조회 사장 답글까지 포함(feat: 리뷰 조회 기능 구현)
    @Transactional(readOnly = true)
    public ReviewResponse getReview(UUID reviewId){
        Review review = findActiveReview(reviewId);
        ReviewCommentResponse ownerReply = reviewCommentRepository
                .findByReview_IdAndDeletedAtIsNull(reviewId)
                .map(ReviewCommentResponse::new)
                .orElse(null);
        return new ReviewResponse(review, ownerReply);
    }
    //목록 가게 단위 (feat: 리뷰 조회 기능 구현)
    @Transactional(readOnly = true)
    public Page<ReviewListResponse> getStoreReviews(UUID storeId, Pageable pageable){
        return reviewRepository.findByStore_IdAndDeletedAtIsNullOrderByCreatedAtDesc(storeId, pageable)
                .map(ReviewListResponse::new);
    }

    //수정 본인만 (feat: 리뷰 수정 기능 구현)
    @Transactional
    public ReviewResponse updateReview(
            User loginUser,
            UUID reviewId,
            ReviewUpdateRequest request
    ){
      Review review = findActiveReview(reviewId);
      if (!review.isOwner(loginUser.getId())){
          throw new CustomException(ErrorCode.REVIEW_NOT_OWNER);
      }
      review.update(request.star(), request.content());
      recalculateStoreRating(review.getStore());
      return new ReviewResponse(review);
    }

    //삭제 본인만, soft-delete (feat: 리뷰삭제 기능 구현)
    @Transactional
    public void deleteReview(User loginUser, UUID reviewId){
        Review review = findActiveReview(reviewId);
        if (!review.isOwner(loginUser.getId())){
            throw new CustomException(ErrorCode.REVIEW_NOT_OWNER);
        }
        review.softDelete(loginUser.getId());
        recalculateStoreRating(review.getStore());
    }

    private Review findActiveReview(UUID reviewId){
        return reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
    }

    private void recalculateStoreRating(Store store){
        double avg = reviewRepository.findAverageStarByStoreId(store.getId());
        long count = reviewRepository.countByStore_IdAndDeletedAtIsNull(store.getId());
        BigDecimal averageRating = BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);
        store.updateRating(averageRating, (int) count);
    }









}
