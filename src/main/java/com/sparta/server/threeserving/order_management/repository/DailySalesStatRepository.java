package com.sparta.server.threeserving.order_management.repository;

import com.sparta.server.threeserving.order_management.entity.DailySalesStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface DailySalesStatRepository extends JpaRepository<DailySalesStat, UUID> {

    List<DailySalesStat> findByStoreIdAndStatDateBetweenOrderByStatDate(UUID storeId, LocalDate startDate, LocalDate endDate);
}