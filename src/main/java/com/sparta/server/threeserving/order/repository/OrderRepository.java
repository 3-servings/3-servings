package com.sparta.server.threeserving.order.repository;

import com.sparta.server.threeserving.order.entity.OrderStatusEnum;
import com.sparta.server.threeserving.order.entity.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Orders, UUID> {
    // getForCustomer
    Page<Orders> findAllByUserIdAndStoreIdAndOrderStatusAndDeletedAtIsNull(
            Long userId, UUID storeId, OrderStatusEnum orderStatus, Pageable pageable);

    Page<Orders> findAllByUserIdAndStoreIdAndDeletedAtIsNull(
            Long userId, UUID storeId, Pageable pageable);

    Page<Orders> findAllByUserIdAndOrderStatusAndDeletedAtIsNull(
            Long userId, OrderStatusEnum orderStatus, Pageable pageable);

    Page<Orders> findAllByUserIdAndDeletedAtIsNull(
            Long userId, Pageable pageable);

    Page<Orders> findByStoreIdAndOrderStatusAndDeletedAtIsNull(
            UUID storeId, OrderStatusEnum status, Pageable pageable);

    // getForAdmin
    Page<Orders> findByStoreIdAndDeletedAtIsNull(
            UUID storeId, Pageable pageable);

    Page<Orders> findByOrderStatusAndDeletedAtIsNull(
            OrderStatusEnum status, Pageable pageable);

    Page<Orders> findByDeletedAtIsNull(
            Pageable pageable);

    // getForOwner
    Page<Orders> findByStoreIdInAndOrderStatusAndDeletedAtIsNull(
            List<UUID> ownedStoreIdList, OrderStatusEnum status, Pageable pageable);

    Page<Orders> findByStoreIdInAndDeletedAtIsNull(
            List<UUID> ownedStoreIdList, Pageable pageable);
}
