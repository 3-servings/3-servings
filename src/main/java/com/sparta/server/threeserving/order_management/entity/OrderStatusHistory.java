package com.sparta.server.threeserving.order_management.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "p_order_status_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderStatusHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private OrderStatusEnum previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 20)
    private OrderStatusEnum currentStatus;

    @Column(name = "memo")
    private String memo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_management_id", nullable = false)
    private OrderManagement orderManagement;


    public OrderStatusHistory(OrderManagement orderManagement,OrderStatusEnum previousStatus,OrderStatusEnum currentStatus) {
        this.orderManagement = orderManagement;
        this.previousStatus = previousStatus;
        this.currentStatus = currentStatus;
        this.memo = orderManagement.getRejectMemo();
    }

}
