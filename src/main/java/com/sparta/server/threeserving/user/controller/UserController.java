package com.sparta.server.threeserving.user.controller;

import com.sparta.server.threeserving.auth.UserDetailsImpl;
import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.user.dto.UserResponse;
import com.sparta.server.threeserving.user.dto.UserUpdateRequest;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import com.sparta.server.threeserving.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 페이지 노출 건수는 10/30/50 만 허용, 그 외는 10으로 고정
    private static final Set<Integer> ALLOWED_SIZES = Set.of(10,30,50);

    //정렬 허용 필드 생성일 순.
    private static final Set<String> ALLOWED_SORTS = Set.of("createdAt", "username", "nickname");


    //토큰으로 내 정보 조회  GET /api/users/my
    @GetMapping("/my")
    public ApiResponse<UserResponse> getMyInfo(
            @AuthenticationPrincipal UserDetailsImpl userDetails
            ){
        Long userId = requireLogin(userDetails);
        return ApiResponse.success(SuccessCode.SUCCESS, userService.getMyInfo(userId));
    }

    // UPDATE : 내 프로필 수정  PATCH /api/users/me
    @PatchMapping("/my")
    public ApiResponse<UserResponse> updateMyInfo(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid UserUpdateRequest request
    ) {
        Long userId = requireLogin(userDetails);
        return ApiResponse.success(SuccessCode.UPDATED, userService.updateProfile(userId, request));
    }

    // SEARCH 회원 목록 (관리자 MASTER/MANAGER) Get /api/users?/role=&keyword=&page=&size=&sort=&direction=
    @GetMapping
    public ApiResponse<Page<UserResponse>> searchUsers(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false)UserRoleEnum role,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
            ){
        requireAdmin(userDetails);
        Pageable pageable = resolvePageable(page,size,sort,direction);
        return ApiResponse.success(SuccessCode.SUCCESS, userService.searchUsers(role,keyword,pageable));
    }


















    private Long requireLogin(UserDetailsImpl userDetails){
        if (userDetails == null) throw new CustomException(ErrorCode.UNAUTHENTICATED);
        return userDetails.getUser().getId();
    }

    private void requireAdmin(UserDetailsImpl userDetails){
        if (userDetails == null) throw new CustomException(ErrorCode.UNAUTHENTICATED);
        UserRoleEnum role = userDetails.getUser().getRole();
        if (role != UserRoleEnum.MASTER && role != UserRoleEnum.MANAGER){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    private Pageable resolvePageable(int page, int size, String sort, String direction) {
        int resolvedSize = ALLOWED_SIZES.contains(size) ? size : 10;   // 10/30/50 외에는 10 고정
        String sortProp = ALLOWED_SORTS.contains(sort) ? sort : "createdAt"; // 기본 생성일순
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(Math.max(page, 0), resolvedSize, Sort.by(dir, sortProp));
    }



}
