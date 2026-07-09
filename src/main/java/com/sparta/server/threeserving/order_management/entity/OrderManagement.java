package com.sparta.server.threeserving.order_management.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order.entity.Orders;
import com.sparta.server.threeserving.store.entity.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "p_order_management")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderManagement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatusEnum orderStatusEnum;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders orders;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reject_reason_id")
    private RejectReasonCode rejectReasonCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;


    public OrderManagement(Orders order, Store store,OrderStatusEnum status) {
        this.orders = order;
        this.store = store;
        this.orderStatusEnum = status;
    }

    //승인
    public void accept(Integer estimatedCookTime) {

        validatePending();

        this.orderStatusEnum = OrderStatusEnum.ACCEPTED;
        this.estimatedCookTime = estimatedCookTime;
        this.acceptedAt = OffsetDateTime.now();
    }

    //거절
    public void reject(RejectReasonCode rejectReasonCode, String memo) {

        validatePending();

        this.orderStatusEnum = OrderStatusEnum.REJECTED;
        this.rejectReasonCode = rejectReasonCode;
        this.rejectMemo=memo;
        this.rejectedAt = OffsetDateTime.now();
    }

    //단순 주문 상태 변환
    public void changeStatus(OrderStatusEnum status) {

        validateStatusTransition(status);

        this.orderStatusEnum = status;
        switch (status) {
            case ACCEPTED -> this.acceptedAt = OffsetDateTime.now();
            case COOKING -> this.cookingStartedAt = OffsetDateTime.now();
            case READY -> this.readyAt = OffsetDateTime.now();
            case COMPLETED -> this.completedAt = OffsetDateTime.now();
        }
    }


    private void validatePending() {
        if (this.orderStatusEnum != OrderStatusEnum.PENDING) {
            throw new CustomException(
                    ErrorCode.ORDER_STATUS_TRANSITION_INVALID
            );
        }
    }


    private void validateStatusTransition(OrderStatusEnum status) {
        if (!this.orderStatusEnum.canTransitionTo(status)) {
            throw new CustomException(ErrorCode.ORDER_STATUS_TRANSITION_INVALID);
        }
    }

//    private void validateStatusTransition(OrderStatusEnum status) {
//
//        if (this.orderStatusEnum == OrderStatusEnum.PENDING
//                && status != OrderStatusEnum.ACCEPTED
//                && status != OrderStatusEnum.REJECTED) {
//
//            throw new CustomException(
//                    ErrorCode.ORDER_STATUS_TRANSITION_INVALID
//            );
//        }
//
//        if (this.orderStatusEnum == OrderStatusEnum.ACCEPTED
//                && status != OrderStatusEnum.COOKING) {
//
//            throw new CustomException(
//                    ErrorCode.ORDER_STATUS_TRANSITION_INVALID
//            );
//        }
//
//        if (this.orderStatusEnum == OrderStatusEnum.COOKING
//                && status != OrderStatusEnum.READY) {
//
//            throw new CustomException(
//                    ErrorCode.ORDER_STATUS_TRANSITION_INVALID
//            );
//        }
//    }
}
