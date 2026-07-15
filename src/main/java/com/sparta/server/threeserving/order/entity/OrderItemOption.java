package com.sparta.server.threeserving.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "p_order_item_option")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@Builder
public class OrderItemOption {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name="order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(name="option_item_id", nullable = false)
    private UUID optionItemId;

    @Column(name="option_name", nullable = false)
    private String optionName;

    @Column(name="additional_price", nullable = false)
    @Min(value = 0)
    @Builder.Default
    private Integer additionalPrice = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    public OrderItemOption(OrderItem orderItem, UUID optionItemId, String optionName, Integer additionalPrice) {
        this.orderItem = orderItem;
        this.optionItemId = optionItemId;
        this.optionName = optionName;
        this.additionalPrice = additionalPrice;
    }
}
