package com.sparta.server.threeserving.user.dto;

import com.sparta.server.threeserving.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private final Long id;
    private final String username;
    private final String nickname;
    private final String email;
    private final String role;
    private final String loginType;
    private final String phone;

    public static UserResponse from(User user){
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .role(user.getRole().name())
                .loginType(user.getLoginType().name())
                .phone(user.getPhone())
                .build();
    }
}
