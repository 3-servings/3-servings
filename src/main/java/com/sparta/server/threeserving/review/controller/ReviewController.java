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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    private static final Set<Integer> ALLOWED_SIZES = Set.of(10, 30, 50);
    private static final Set<String> ALLOWED_SORTS = Set.of("createdAt", "star");

    // 이미지 프리사인 URL 발급은 공용 이미지 API(POST /api/v1/images/presigned-url, domainType=REVIEW) 사용

    // 검색 GET /api/reviews?storeId=&minStar=&keyword=&page=&size=&sort=&direction=
    @GetMapping("/reviews")
    public ApiResponse<Page<ReviewListResponse>> searchReviews(
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) Integer minStar,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ){
        Pageable pageable = resolvePageable(page, size, sort, direction);
        return ApiResponse.success(SuccessCode.SUCCESS,
                reviewService.searchReviews(storeId, minStar, keyword, pageable));
    }

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

    //가게 목록 /api/store/{storeId}/reviews  경로변수
    @GetMapping("/stores/{storeId}/reviews")
    public ApiResponse<Page<ReviewListResponse>> getStoreReviews(
            @PathVariable UUID storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ){
        Pageable pageable = resolvePageable(page, size, sort, direction);
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

    // 10/30/50 외 size는 10 고정, 정렬은 기본 생성일순
    private Pageable resolvePageable(int page, int size, String sort, String direction){
        int resolvedSize = ALLOWED_SIZES.contains(size) ? size : 10;
        String sortProp = ALLOWED_SORTS.contains(sort) ? sort : "createdAt";
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(Math.max(page, 0), resolvedSize, Sort.by(dir, sortProp));
    }
}
