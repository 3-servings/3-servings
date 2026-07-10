package com.sparta.server.threeserving.payment.repository;

import com.sparta.server.threeserving.order.entity.Orders;
import com.sparta.server.threeserving.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findByTransactionId(String transactionId);
}
