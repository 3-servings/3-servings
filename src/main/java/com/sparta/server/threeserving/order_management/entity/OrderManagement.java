package com.sparta.server.threeserving.order_management.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order.entity.Orders;
import com.sparta.server.threeserving.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "p_order_management")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderManagement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

//    @Column(name = "store_id", nullable = false)
//    private UUID storeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatusEnum orderStatus;

    @Column(name = "estimated_cook_time")
    private Integer estimatedCookTime;

    @Column(name = "reject_memo")
    private String rejectMemo;

    @Column(name = "accepted_at")
    private OffsetDateTime acceptedAt;

    @Column(name = "cooking_started_at")
    private OffsetDateTime cookingStartedAt;

    @Column(name = "ready_at")
    private OffsetDateTime readyAt;

    @Column(name = "completed_at")
    @Temporal(TemporalType.TIMESTAMP)
    private OffsetDateTime completedAt;

    @Column(name = "rejected_at")
    private OffsetDateTime rejectedAt;

    @Column(name = "cancled_at")
    private OffsetDateTime cancledAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders orders;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reject_reason_id")
    private RejectReasonCode rejectReasonCode;


    public OrderManagement(Orders order, Store store, OrderStatusEnum status) {
        this.orders = order;
        this.store = store;
        this.orderStatus = status;
    }

    //승인
    public void accept(Integer estimatedCookTime) {

        validateStatusTransition(OrderStatusEnum.ACCEPTED);

        this.orderStatus = OrderStatusEnum.ACCEPTED;
        this.estimatedCookTime = estimatedCookTime;
        this.acceptedAt = OffsetDateTime.now();
    }

    //거절
    public void reject(RejectReasonCode rejectReasonCode, String memo) {

        validateStatusTransition(OrderStatusEnum.REJECTED);

        this.orderStatus = OrderStatusEnum.REJECTED;
        this.rejectReasonCode = rejectReasonCode;
        this.rejectMemo=memo;
        this.rejectedAt = OffsetDateTime.now();
    }

    //단순 주문 상태 변환
    public void changeStatus(OrderStatusEnum status) {

        validateStatusTransition(status);

        this.orderStatus = status;
        switch (status) {
            case ACCEPTED -> this.acceptedAt = OffsetDateTime.now();
            case COOKING -> this.cookingStartedAt = OffsetDateTime.now();
            case READY -> this.readyAt = OffsetDateTime.now();
            case COMPLETED -> this.completedAt = OffsetDateTime.now();
            case CANCELED -> this.cancledAt = OffsetDateTime.now();
        }
    }

    //조리시간 변경
    public void changeCookingTime(Integer estimatedCookTime) {

        this.estimatedCookTime = estimatedCookTime;
    }


    private void validateStatusTransition(OrderStatusEnum status) {
        if (!this.orderStatus.canTransitionTo(status)) {
            throw new CustomException(ErrorCode.ORDER_STATUS_TRANSITION_INVALID);
        }
    }


}
