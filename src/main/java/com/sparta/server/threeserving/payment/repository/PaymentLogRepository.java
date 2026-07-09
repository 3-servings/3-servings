package com.sparta.server.threeserving.payment.repository;

import com.sparta.server.threeserving.payment.entity.Payment;
import com.sparta.server.threeserving.payment.entity.PaymentLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentLogRepository extends JpaRepository<PaymentLog, UUID> {
    List<PaymentLog> findAllByPayment(Payment payment);
}
