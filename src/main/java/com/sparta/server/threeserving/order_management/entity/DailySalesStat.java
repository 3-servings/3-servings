package com.sparta.server.threeserving.order_management.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import com.sparta.server.threeserving.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Entity
@Table(name = "p_daily_sales_stat")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailySalesStat extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "stat_date")
    private LocalDate statDate;

    @Column(name = "total_order_count")
    private Long totalOrderCount;

    @Column(name = "completed_count")
    private Long completedCount;

    @Column(name = "rejected_count")
    private Long rejectedCount;

    @Column(name = "canceled_count")
    private Long canceledCount;

    @Column(name = "total_sales_amount")
    private Long totalSalesAmount;

    @Column(name = "avg_cook_time")
    private BigDecimal avgCookTime;

    @Column(name = "completed_rate")
    private BigDecimal completedRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;


//    public DailySalesStat(
//            Store store,
//            LocalDate statDate,
//            Integer totalOrderCount,
//            Integer completedCount,
//            Integer rejectedCount,
//            Integer canceledCount,
//            Long totalSalesAmount
//    ) {
//        this.store = store;
//        this.statDate = statDate;
//        this.totalOrderCount = totalOrderCount;
//        this.completedCount = completedCount;
//        this.rejectedCount = rejectedCount;
//        this.canceledCount = canceledCount;
//        this.totalSalesAmount = totalSalesAmount;
//    }



}