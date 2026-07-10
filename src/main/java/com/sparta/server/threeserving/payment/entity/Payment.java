package com.sparta.server.threeserving.payment.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import com.sparta.server.threeserving.order.entity.Orders;
import com.sparta.server.threeserving.payment.dto.request.PaymentRequest;
import com.sparta.server.threeserving.payment.enums.PaymentMethod;
import com.sparta.server.threeserving.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "p_payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Orders order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.CARD;

    @Column(nullable = false)
    @Builder.Default
    private Long amount = 0L;

    @Column(nullable = false, unique = true)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.SUCCESS;

    private Instant requestedAt;

    private Instant approvedAt;

    private Instant refundAt;

    public static Payment create(Orders order, PaymentRequest request){
        Instant now = Instant.now();

        return Payment.builder()
                .order(order)
                .paymentMethod(request.getPaymentMethod())
                .amount((long) order.getTotalPrice())
                .transactionId(UUID.randomUUID().toString())
                .status(PaymentStatus.SUCCESS)
                .requestedAt(now)
                .build();
    }

    public void refund(){
        this.status = PaymentStatus.REFUNDED;
        this.refundAt = Instant.now();
    }
}
