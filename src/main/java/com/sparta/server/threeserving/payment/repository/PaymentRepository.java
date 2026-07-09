package com.sparta.server.threeserving.payment.repository;

import com.sparta.server.threeserving.order.entity.Orders;
import com.sparta.server.threeserving.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrder(Orders order);

    Optional<Payment> findByTransactionId(String transactionId);
}
