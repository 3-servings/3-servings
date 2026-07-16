package com.sparta.server.threeserving.review.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.image.enums.DomainType;
import com.sparta.server.threeserving.image.service.ImageService;
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
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j // 로그
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;   // 가게 연관 세팅 + 평점 재계산
    private final ReviewCommentRepository reviewCommentRepository;
    private final OrderRepository orderRepository;
    private final ImageService imageService;   // 팀 공용 이미지 도메인 (image.service.ImageService)

    // 프리사인 URL 발급은 공용 이미지 도메인의 POST /api/v1/images/presigned-url (DomainType=REVIEW) 사용

    // 작성: COMPLETED 주문 + 본인 주문만
    @Transactional
    public ReviewResponse createReview(User loginUser, ReviewCreateRequest request) {
        Orders order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_ORDER_OWNER_OF_REVIEW);
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
        try {
            reviewRepository.saveAndFlush(review);
        } catch (DataIntegrityViolationException e) {
            // 동시 요청 방어 (uk_review_order_active 유니크 인덱스 필요)
            log.warn("리뷰 중복 작성 경합 감지 : orderId={}, userId={}", order.getId(), loginUser.getId());
            throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        List<String> imageUrls = imageService.saveImages(DomainType.REVIEW, review.getId(), request.images());
        recalculateStoreRating(store);

        log.info("리뷰 작성 완료 : reviewId={}, userId={}, storeId={}, star={}, imageCount={}",
                review.getId(), loginUser.getId(), store.getId(), review.getStar(), imageUrls.size());
        return new ReviewResponse(review, imageUrls);
    }

    // 상세 조회 (사장 답글 포함)
    @Transactional(readOnly = true)
    public ReviewResponse getReview(UUID reviewId) {
        Review review = findActiveReview(reviewId);
        List<String> imageUrls = imageService.getImageUrls(DomainType.REVIEW, reviewId);
        ReviewCommentResponse ownerReply = reviewCommentRepository
                .findByReview_IdAndDeletedAtIsNull(reviewId)
                .map(ReviewCommentResponse::new)
                .orElse(null);
        log.info("리뷰 상세 조회 : reviewId={}", reviewId);
        return new ReviewResponse(review, imageUrls, ownerReply);
    }

    //목록 가게 단위 (feat: 리뷰 조회 기능 구현)
    @Transactional(readOnly = true)
    public Page<ReviewListResponse> getStoreReviews(UUID storeId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByStore_IdAndDeletedAtIsNullOrderByCreatedAtDesc(storeId, pageable);
        List<UUID> reviewIds = reviews.getContent().stream().map(Review::getId).toList();
        Map<UUID, String> thumbnails = imageService.getImageUrlMap(DomainType.REVIEW, reviewIds); // N+1 방지
        return reviews.map(r -> new ReviewListResponse(r, thumbnails.get(r.getId())));
    }

    // 수정: 본인만
    @Transactional
    public ReviewResponse updateReview(User loginUser, UUID reviewId, ReviewUpdateRequest request) {
        Review review = findActiveReview(reviewId);
        if (!review.isOwner(loginUser.getId())) {
            throw new CustomException(ErrorCode.REVIEW_NOT_OWNER);
        }
        // 이미지 교체가 영속성 컨텍스트를 비우므로(@Modifying(clearAutomatically=true)) 먼저 수행한다
        boolean imagesReplaced = request.images() != null && !request.images().isEmpty();
        List<String> imageUrls = imagesReplaced
                ? imageService.replaceImages(DomainType.REVIEW, reviewId, request.images(), loginUser.getId())
                : imageService.getImageUrls(DomainType.REVIEW, reviewId);

        Review target = imagesReplaced ? findActiveReview(reviewId) : review;   // 재조회

        // 안 보냄 = 변경 없음
        String newContent = request.content() != null ? request.content() : target.getContent();
        target.update(request.star(), newContent);

        recalculateStoreRating(target.getStore());
        log.info("리뷰 수정 완료 : reviewId={}, userId={}", reviewId, loginUser.getId());
        return new ReviewResponse(target, imageUrls);
    }

    // 삭제: 작성자 본인 또는 관리자, soft-delete
    @Transactional
    public void deleteReview(User loginUser, UUID reviewId) {
        Review review = findActiveReview(reviewId);
        if (!review.isOwner(loginUser.getId()) && !isAdmin(loginUser)) {
            throw new CustomException(ErrorCode.REVIEW_NOT_OWNER);
        }

        // 이미지 벌크 삭제가 영속성 컨텍스트를 비우므로(@Modifying(clearAutomatically=true)) 먼저 수행한다
        imageService.softDeleteImages(DomainType.REVIEW, reviewId, loginUser.getId());

        Review target = findActiveReview(reviewId);   // 재조회
        target.softDelete(loginUser.getId());

        recalculateStoreRating(target.getStore());
        log.info("리뷰 삭제 완료 : reviewId={}, userId={}", reviewId, loginUser.getId());
    }

    // 검색: 쿼리스트링 storeId/minStar/keyword + 정렬/페이지
    @Transactional(readOnly = true)
    public Page<ReviewListResponse> searchReviews(UUID storeId, Integer minStar, String keyword, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword;
        log.info("리뷰 검색 : storeId={}, minStar={}, keyword={}, page={}, size={}",
                storeId, minStar, kw, pageable.getPageNumber(), pageable.getPageSize());
        Page<Review> reviews = reviewRepository.search(storeId, minStar, kw, pageable);
        List<UUID> reviewIds = reviews.getContent().stream().map(Review::getId).toList();
        Map<UUID, String> thumbnails = imageService.getImageUrlMap(DomainType.REVIEW, reviewIds);
        return reviews.map(r -> new ReviewListResponse(r, thumbnails.get(r.getId())));
    }

    private Review findActiveReview(UUID reviewId){
        return reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
    }

    // 관리자 여부 (운영 개입: 삭제만 허용)
    private boolean isAdmin(User user) {
        UserRoleEnum role = user.getRole();
        return role == UserRoleEnum.MASTER || role == UserRoleEnum.MANAGER;
    }

    private void recalculateStoreRating(Store store) {
        double avg = reviewRepository.findAverageStarByStoreId(store.getId());
        long count = reviewRepository.countByStore_IdAndDeletedAtIsNull(store.getId());
        BigDecimal averageRating = BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);
        store.updateRating(averageRating, (int) count);
    }

}
