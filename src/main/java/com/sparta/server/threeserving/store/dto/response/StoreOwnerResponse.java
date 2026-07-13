package com.sparta.server.threeserving.store.dto.response;

import com.sparta.server.threeserving.user.entity.User;
import lombok.Getter;

@Getter
public class StoreOwnerResponse {
    Long id;
    String nickname;

    public StoreOwnerResponse(User user){
        this.id = user.getId();
        this.nickname = user.getNickname();
    }
}
