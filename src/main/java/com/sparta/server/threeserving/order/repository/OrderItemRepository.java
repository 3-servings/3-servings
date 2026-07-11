package com.sparta.server.threeserving.order.repository;

import com.sparta.server.threeserving.order.entity.OrderItem;
import com.sparta.server.threeserving.order.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> findAllByOrderAndDeletedAtIsNull(Orders order);

}
