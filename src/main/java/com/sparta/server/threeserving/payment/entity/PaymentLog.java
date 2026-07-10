package com.sparta.server.threeserving.payment.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import com.sparta.server.threeserving.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "p_payment_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PaymentLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(length = 100)
    private String message;

    public static PaymentLog create(
            Payment payment,
            PaymentStatus status,
            String message
    ){
        return PaymentLog.builder()
                .payment(payment)
                .status(status)
                .message(message)
                .build();
    }
}
