package com.sparta.server.threeserving.order_management.repository;


import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order_management.entity.OrderManagement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderManagementRepository extends JpaRepository<OrderManagement, UUID> {
    Page<OrderManagement>  findByStoreId(UUID storeId, Pageable pageable);

    Page<OrderManagement>  findByStoreIdAndOrderStatus(UUID storeId, OrderStatusEnum orderStatus, Pageable pageable);

}
