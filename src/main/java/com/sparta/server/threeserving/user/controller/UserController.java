package com.sparta.server.threeserving.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.server.threeserving.auth.UserDetailsImpl;
import com.sparta.server.threeserving.auth.cookie.CookieUtil;
import com.sparta.server.threeserving.auth.jwt.JwtUtil;
import com.sparta.server.threeserving.auth.jwt.TokenService;
import com.sparta.server.threeserving.auth.kakao.KakaoService;
import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.user.dto.LoginResult;
import com.sparta.server.threeserving.user.dto.SignupRequest;
import com.sparta.server.threeserving.user.dto.UserResponse;
import com.sparta.server.threeserving.user.dto.WithdrawRequest;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import com.sparta.server.threeserving.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;
    private final KakaoService kakaoService;

    public UserController(UserService userService, TokenService tokenService, KakaoService kakaoService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.kakaoService = kakaoService;
    }

    @PostMapping("/signup/customer")
    public ResponseEntity<UserResponse> signupCustomer(
            @Valid @RequestBody SignupRequest request) {
        // Role을 서버가 고정 → 클라이언트가 개입할 여지 없음
        return ResponseEntity.ok(userService.signup(request, UserRoleEnum.CUSTOMER));
    }

    @PostMapping("/signup/owner")
    public ResponseEntity<UserResponse> signupOwner(
            @Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(userService.signup(request, UserRoleEnum.OWNER));
    }


    @PostMapping("/re-issue")
    public ResponseEntity<ApiResponse<Void>> reissue(
            HttpServletRequest request,
            HttpServletResponse response
    ){
        String accessToken = tokenService.reissueAccessToken(request);

        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, accessToken);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SUCCESS));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ){
        tokenService.logout(request, response);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SUCCESS));
    }

    // 카카오 소셜 로그인
    @GetMapping("/user/kakao/login")
    public void redirectToKakao(@RequestParam(defaultValue = "CUSTOMER") String role, HttpServletResponse response) throws IOException {
        response.sendRedirect(kakaoService.getAuthorizeUrl(role));
    }

    @GetMapping("/kakao/call-back")
    public ApiResponse<LoginResult> kakaoCallback(@RequestParam String code,
                                                  @RequestParam(required = false) String state,
                                                  HttpServletResponse response) throws JsonProcessingException {
        LoginResult result = kakaoService.login(code, state);
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, result.getAccessToken());
        CookieUtil.addCookie(response, "refreshToken", result.getRefreshToken(), 60 * 60 * 24 * 14);
        return ApiResponse.success(SuccessCode.SUCCESS, result);
    }


    //회원탈퇴
    @DeleteMapping("/delete")
    public ApiResponse<Void> withdraw(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid WithdrawRequest request){
        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHENTICATED);
        }
        userService.withdraw(userDetails.getUser().getId(), request.password());
        return ApiResponse.success(SuccessCode.DELETED);
    }

}
