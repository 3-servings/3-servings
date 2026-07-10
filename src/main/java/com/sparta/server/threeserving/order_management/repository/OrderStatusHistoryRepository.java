package com.sparta.server.threeserving.order_management.repository;

import com.sparta.server.threeserving.order_management.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, UUID> {


}
