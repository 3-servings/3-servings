package com.sparta.server.threeserving.user.controller;

import com.sparta.server.threeserving.user.dto.SignupRequest;
import com.sparta.server.threeserving.user.dto.UserResponse;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import com.sparta.server.threeserving.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
}
