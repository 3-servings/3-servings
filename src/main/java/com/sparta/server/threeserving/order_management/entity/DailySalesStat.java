package com.sparta.server.threeserving.order_management.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import com.sparta.server.threeserving.store.entity.Store;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "p_daily_sales_stat")
public class DailySalesStat extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "stat_date")
    private LocalDate statDate;

    @Column(name = "total_order_count")
    private Integer totalOrderCount;

    @Column(name = "accepted_count")
    private Integer acceptedCount;

    @Column(name = "rejected_count")
    private Integer rejectedCount;

    @Column(name = "canceled_count")
    private Integer canceledCount;

    @Column(name = "total_sales_amount")
    private Long totalSalesAmount;

    @Column(name = "avg_cook_time")
    private BigDecimal avgCookTime;

    @Column(name = "accept_rate")
    private BigDecimal acceptRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;



}