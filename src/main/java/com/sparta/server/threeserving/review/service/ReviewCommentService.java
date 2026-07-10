package com.sparta.server.threeserving.review.service;


import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.review.dto.ReviewCommentRequest;
import com.sparta.server.threeserving.review.dto.ReviewCommentResponse;
import com.sparta.server.threeserving.review.entity.Review;
import com.sparta.server.threeserving.review.entity.ReviewComment;
import com.sparta.server.threeserving.review.repository.ReviewCommentRepository;
import com.sparta.server.threeserving.review.repository.ReviewRepository;
import com.sparta.server.threeserving.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewCommentService {

    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;



    //사당 답글 작성 고객 리뷰에 대댓글 (feat: 사장답글 작성기능 구현)
    // 리뷰가 존재하면, 리뷰가 달린 가게의 "사장 본인" 인지 체크, 답글 중복방지, 저장
    @Transactional
    public ReviewCommentResponse createReply(User loginUser, UUID reviewId, ReviewCommentRequest request) {
        Review review = findActiveReview(reviewId);

        verifyStoreOwner(review, loginUser);   // 본인 가게 리뷰인지

        if (reviewCommentRepository.existsByReview_IdAndDeletedAtIsNull(reviewId)) {
            throw new CustomException(ErrorCode.REVIEW_COMMENT_ALREADY_EXISTS);   // 리뷰당 1개
        }

        ReviewComment comment = ReviewComment.create(review, loginUser, request.content());
        reviewCommentRepository.save(comment);
        return new ReviewCommentResponse(comment);
    }

    // 사장 답글 수정 (feat: 사장 답글 수정 기능 구현)
    // 답글 존재, 답글 작성 사장 본인, 수정
    @Transactional
    public ReviewCommentResponse updateReply(User loginUser, UUID reviewId, ReviewCommentRequest request){
        ReviewComment comment = reviewCommentRepository.findByReview_IdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_COMMENT_NOT_FOUND));

        if (!comment.isOwner(loginUser.getId())){
            throw new CustomException(ErrorCode.NOT_STORE_OWNER);
        }

        comment.update(request.content());  // 더티체킹
        return new ReviewCommentResponse(comment);
    }


    // ===== private =====
    private Review findActiveReview(UUID reviewId) {
        return reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
    }

    /** 리뷰가 달린 가게의 사장이 loginUser 인지 검증 */
    private void verifyStoreOwner(Review review, User loginUser) {
        Long storeOwnerId = review.getStore().getOwner().getId();
        if (!storeOwnerId.equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_STORE_OWNER);
        }
    }



}
