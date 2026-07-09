package com.sparta.server.threeserving.order.entity;

public enum OrderStatusEnum {
    PENDING,	// 신규 주문, 사장님 응답 대기
    ACCEPTED,	// 수락 완료, 조리 대기
    REJECTED,	// 거절됨
    COOKING,    // 조리 중
    READY,	    // 조리 완료, 픽업/배달 대기
    COMPLETED,	// 주문완료(고객이 수령 완료)
    CANCELED,	// 자동취소(미응답 타임아웃 등)
}
