package com.sparta.server.threeserving.order_management.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
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

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "order_id", nullable = false)
//    private Order order;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "store_id", nullable = false)
//    private Store store;
//
//    public OrderManagement(Order order, Store store) {
//        this.order = order;
//        this.store = store;
//        this.orderStatus = OrderStatus.PENDING;
}
