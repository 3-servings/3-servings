package com.sparta.server.threeserving.review.controller;

import com.sparta.server.threeserving.auth.UserDetailsImpl;
import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.review.dto.ReviewCreateRequest;
import com.sparta.server.threeserving.review.dto.ReviewListResponse;
import com.sparta.server.threeserving.review.dto.ReviewResponse;
import com.sparta.server.threeserving.review.dto.ReviewUpdateRequest;
import com.sparta.server.threeserving.review.service.ReviewService;
import com.sparta.server.threeserving.user.entity.User;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    //작성 POST /api/reviews
    @PostMapping("/reviews")
    public ApiResponse<ReviewResponse> createReview(
            @RequestBody @Valid ReviewCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
            ){

        User loginUser = requireCustomer(userDetails);
        return ApiResponse.success(SuccessCode.CREATED,
                reviewService.createReview(loginUser, request));

    }
    // 상세 GET /api/reviews/{reviewId} 사장 답글 포함
    @GetMapping("/reviews/{reviewId}")
    public ApiResponse<ReviewResponse> getReview(@PathVariable UUID reviewId){
        return ApiResponse.success(SuccessCode.SUCCESS, reviewService.getReview(reviewId));
    }

    //가게 목록 /api/store/{storeId}/reviews
    @GetMapping("/stores/{storeId}/reviews")
    public ApiResponse<Page<ReviewListResponse>> getStoreReviews(
            @PathVariable UUID storeId, Pageable pageable
    ){
        return ApiResponse.success(SuccessCode.SUCCESS,
                reviewService.getStoreReviews(storeId, pageable));

    }

    // PUT 수정 /api/reviews/{reviewId}
    @PutMapping("/reviews/{reviewId}")
    public ApiResponse<ReviewResponse> updateReview(
            @PathVariable UUID reviewId,
            @RequestBody @Valid ReviewUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
            ){

        User loginUser = requireCustomer(userDetails);
        return ApiResponse.success(SuccessCode.UPDATED,
                reviewService.updateReview(loginUser, reviewId, request));
    }


    //삭제 DELETE /api/reviews/{reviewId}
    @DeleteMapping("/reviews/{reviewId}")
    public ApiResponse<Void> deleteReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        User loginUser = requireCustomer(userDetails);
        reviewService.deleteReview(loginUser,reviewId);
        return ApiResponse.success(SuccessCode.DELETED);
    }

    //로그인 고객 권한
    private User requireCustomer(UserDetailsImpl userDetails){
        if (userDetails == null) throw new CustomException(ErrorCode.UNAUTHENTICATED);
        UserRoleEnum role = userDetails.getUser().getRole();
        if (role != UserRoleEnum.CUSTOMER && role != UserRoleEnum.MANAGER && role != UserRoleEnum.MASTER){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        return userDetails.getUser();
    }












}
