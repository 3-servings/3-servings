package com.sparta.server.threeserving.review.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order.entity.Orders;
import com.sparta.server.threeserving.order.repository.OrderRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j // 로그
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;   // 가게 연관 세팅 + 평점 재계산
    private final ReviewCommentRepository reviewCommentRepository;
    private final OrderRepository orderRepository;
    private final ImageService imageService;
    private final S3PresignService s3PresignService;


    // Presigned URL 발급 (리뷰 이미지 업로드 URL)
    public ReviewImagePresignResponse presignReviewImages(ReviewImagePresignRequest request) {
        List<ReviewImagePresignResponse.Item> items = new java.util.ArrayList<>();
        int seq = 1;
        for (ReviewImagePresignRequest.FileMeta f : request.files()) {
            S3PresignService.PresignedItem p =
                    s3PresignService.createPutPresign(ImageService.REVIEW.toLowerCase(), f.originName(), f.contentType());
            items.add(new ReviewImagePresignResponse.Item(p.key(), p.uploadUrl(), p.publicUrl(), seq++));
        }
        return new ReviewImagePresignResponse(items);
    }


    // 작성: COMPLETED 주문 + 본인 주문만
    @Transactional
    public ReviewResponse createReview(User loginUser, ReviewCreateRequest request) {
        Orders order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_ORDER_OWNER);
        }
        if (order.getOrderStatus() != OrderStatusEnum.COMPLETED) {
            throw new CustomException(ErrorCode.ORDER_NOT_COMPLETED);
        }
        if (reviewRepository.existsByOrder_IdAndDeletedAtIsNull(order.getId())) {
            throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Store store = storeRepository.findById(order.getStoreId())
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        Review review = Review.create(order, loginUser, store, request.star(), request.content());
        reviewRepository.save(review);

        List<String> imageUrls = imageService.saveReviewImages(review.getId(), request.images());
        recalculateStoreRating(store);

        log.info("리뷰 작성 완료 : reviewId={}, userId={}, storeId={}, star={}, imageCount={}",
                review.getId(), loginUser.getId(), store.getId(), review.getStar(), imageUrls.size());
        return new ReviewResponse(review, imageUrls);
    }

    // 상세 조회 (사장 답글 포함)
    @Transactional(readOnly = true)
    public ReviewResponse getReview(UUID reviewId) {
        Review review = findActiveReview(reviewId);
        List<String> imageUrls = imageService.getImageUrls(reviewId);
        ReviewCommentResponse ownerReply = reviewCommentRepository
                .findByReview_IdAndDeletedAtIsNull(reviewId)
                .map(ReviewCommentResponse::new)
                .orElse(null);
        log.info("리뷰 상세 조회 : reviewId={}", reviewId);
        return new ReviewResponse(review, imageUrls, ownerReply);
    }


    //목록 가게 단위 (feat: 리뷰 조회 기능 구현)
    @Transactional(readOnly = true)
    public Page<ReviewListResponse> getStoreReviews(UUID storeId, Pageable pageable){
        return reviewRepository.findByStore_IdAndDeletedAtIsNullOrderByCreatedAtDesc(storeId, pageable)
                .map(r -> {
                    List<String> urls = imageService.getImageUrls(r.getId());
                    return new ReviewListResponse(r, urls.isEmpty() ? null : urls.get(0));
                });
    }

    // 수정: 본인만
    @Transactional
    public ReviewResponse updateReview(User loginUser, UUID reviewId, ReviewUpdateRequest request) {
        Review review = findActiveReview(reviewId);
        if (!review.isOwner(loginUser.getId())) {
            throw new CustomException(ErrorCode.REVIEW_NOT_OWNER);
        }
        review.update(request.star(), request.content());

        List<String> imageUrls;
        if (request.images() != null && !request.images().isEmpty()) {
            imageService.deleteImages(reviewId);
            imageUrls = imageService.saveReviewImages(reviewId, request.images());
        } else {
            imageUrls = imageService.getImageUrls(reviewId);
        }

        recalculateStoreRating(review.getStore());
        log.info("리뷰 수정 완료 : reviewId={}, userId={}", reviewId, loginUser.getId());
        return new ReviewResponse(review, imageUrls);
    }

    // 삭제: 본인만, soft-delete
    @Transactional
    public void deleteReview(User loginUser, UUID reviewId) {
        Review review = findActiveReview(reviewId);
        if (!review.isOwner(loginUser.getId())) {
            throw new CustomException(ErrorCode.REVIEW_NOT_OWNER);
        }
        review.softDelete(loginUser.getId());
        imageService.deleteImages(reviewId);
        recalculateStoreRating(review.getStore());
        log.info("리뷰 삭제 완료 : reviewId={}, userId={}", reviewId, loginUser.getId());
    }

    private Review findActiveReview(UUID reviewId){
        return reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
    }

    private void recalculateStoreRating(Store store) {
        double avg = reviewRepository.findAverageStarByStoreId(store.getId());
        long count = reviewRepository.countByStore_IdAndDeletedAtIsNull(store.getId());
        BigDecimal averageRating = BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);
        store.updateRating(averageRating, (int) count);
    }

}
