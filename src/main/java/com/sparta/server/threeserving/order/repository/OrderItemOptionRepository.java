package com.sparta.server.threeserving.order.repository;

import com.sparta.server.threeserving.order.entity.OrderItem;
import com.sparta.server.threeserving.order.entity.OrderItemOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderItemOptionRepository extends JpaRepository<OrderItemOption, UUID> {
    List<OrderItemOption> findAllByOrderItemIn(List<OrderItem> orderItemList);
}
