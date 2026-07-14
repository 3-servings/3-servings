package com.sparta.server.threeserving.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
public class LoginResult {
    String accessToken;
    String refreshToken;

    public LoginResult(String accessToken, String refreshToken){
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
