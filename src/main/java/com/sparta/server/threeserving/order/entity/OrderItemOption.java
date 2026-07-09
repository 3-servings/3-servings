package com.sparta.server.threeserving.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "p_order_item_option")
@NoArgsConstructor
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
    private Integer additionalPrice;

    @Column(name="quantity", nullable = false)
    @Min(value = 1)
    private Integer quantity;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    public OrderItemOption(OrderItem orderItem, UUID optionItemId, String optionName, Integer additionalPrice, Integer quantity) {
        this.orderItem = orderItem;
        this.optionItemId = optionItemId;
        this.optionName = optionName;
        this.additionalPrice = additionalPrice;
        this.quantity = quantity;
    }
}
