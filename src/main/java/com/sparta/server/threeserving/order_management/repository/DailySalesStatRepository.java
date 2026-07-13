package com.sparta.server.threeserving.order_management.repository;

import com.sparta.server.threeserving.order_management.entity.DailySalesStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DailySalesStatRepository extends JpaRepository<DailySalesStat, UUID> {

}