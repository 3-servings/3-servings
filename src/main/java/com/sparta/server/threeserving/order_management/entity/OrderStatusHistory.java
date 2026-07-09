package com.sparta.server.threeserving.order_management.entity;

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
public class OrderStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "previous_status", length = 20)
    private String previousStatus;

    @Column(name = "current_status", nullable = false, length = 20)
    private String currentStatus;

    @Column(name = "memo")
    private String memo;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_management_id", nullable = false)
    private OrderManagement orderManagement;

}
