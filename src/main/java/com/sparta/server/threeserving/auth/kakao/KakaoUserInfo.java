package com.sparta.server.threeserving.auth.kakao;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoUserInfo {
    private Long id;
    private String nickname;
    private String email;

    public KakaoUserInfo(Long id, String nickname, String email){
        this.id = id;
        this.nickname = nickname;
        this.email = email;
    }
}
