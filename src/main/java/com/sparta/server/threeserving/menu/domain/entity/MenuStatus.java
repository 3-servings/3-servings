package com.sparta.server.threeserving.menu.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MenuStatus {

    AVAILABLE("판매 중"),
    SOLD_OUT("품절"),
    HIDDEN("숨김 처리");

    private final String description;
}
