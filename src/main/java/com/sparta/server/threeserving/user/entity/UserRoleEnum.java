package com.sparta.server.threeserving.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRoleEnum {
    CUSTOMER(Authority.CUSTOMER),   //유저
    OWNER(Authority.OWNER),         //사장
    MANAGER(Authority.MANAGER),     //매니저(선택)
    MASTER(Authority.MASTER);       //관리자

    private final String authority;

    public static class Authority {
        public static final String CUSTOMER = "ROLE_CUSTOMER";
        public static final String OWNER = "ROLE_OWNER";
        public static final String MANAGER = "ROLE_MANAGER";
        public static final String MASTER = "ROLE_MASTER";
    }
}
