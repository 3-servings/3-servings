package com.sparta.server.threeserving.order.entity;

import java.util.Set;

public enum OrderStatusEnum {

    CANCELED(Set.of()),         // 자동취소(미응답 타임아웃 등)
    COMPLETED(Set.of()),        // 주문완료(고객이 수령 완료)
    REJECTED(Set.of()),         // 조리 완료, 픽업/배달 대기
    READY(Set.of(COMPLETED)),   // 조리 중
    COOKING(Set.of(READY)),     // 거절됨
    ACCEPTED(Set.of(COOKING)),  // 수락 완료, 조리 대기
    PENDING(Set.of(ACCEPTED, REJECTED));// 신규 주문, 사장님 응답 대기

    private final Set<OrderStatusEnum> nextStatuses;

    OrderStatusEnum(Set<OrderStatusEnum> nextStatuses) {
        this.nextStatuses = nextStatuses;
    }

    public boolean canTransitionTo(OrderStatusEnum next) {
        return nextStatuses.contains(next);
    }
}
