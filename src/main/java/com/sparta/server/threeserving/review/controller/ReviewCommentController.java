package com.sparta.server.threeserving.review.controller;

import com.sparta.server.threeserving.auth.UserDetailsImpl;
import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.review.dto.ReviewCommentRequest;
import com.sparta.server.threeserving.review.dto.ReviewCommentResponse;
import com.sparta.server.threeserving.review.service.ReviewCommentService;
import com.sparta.server.threeserving.user.entity.User;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReviewCommentController {


    private final ReviewCommentService reviewCommentService;

    //사장 답글 작성 POST /api/reviews/{reviewId}/comment
    @PostMapping("/reviews/{reviewId}/comment")
    public ApiResponse<ReviewCommentResponse> createReply(
            @PathVariable UUID reviewId,
            @RequestBody @Valid ReviewCommentRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
            ){

        // 사장 권한 확인 가게 소유 검증은 서비스에 존재
        User loginUser = requireOwner(userDetails);
        return ApiResponse.success(SuccessCode.CREATED,
                reviewCommentService.createReply(loginUser, reviewId, request));
    }

    // 사장 답글 수정 PUT /api/reviews/{reviewId}/comment
    @PutMapping("/reviews/{reviewId}/comment")
    public ApiResponse<ReviewCommentResponse> updateReply(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewCommentRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        User loginUser = requireOwner(userDetails);
        return ApiResponse.success(SuccessCode.UPDATED,
                reviewCommentService.updateReply(loginUser, reviewId, request));
    }

    //로그인 사장 권한
    private User requireOwner(UserDetailsImpl userDetails){
        if (userDetails == null) throw new CustomException(ErrorCode.UNAUTHENTICATED);
        UserRoleEnum role = userDetails.getUser().getRole();
        if (role != UserRoleEnum.OWNER && role != UserRoleEnum.MASTER){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        return userDetails.getUser();
    }










}
