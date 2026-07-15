package com.sparta.server.threeserving.order.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.UUID;

@Getter
@Entity
@Table(name = "p_order_item")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="order_id", nullable = false)
    private Orders order;

    @Column(name="menu_id", nullable = false)
    private UUID menuId;

    @Column(name="menu_name", nullable = false)
    private String menuName;

    @Column(name="price", nullable = false)
    @Min(value = 0)
    @Builder.Default
    private Integer price = 0;

    @Column(name="quantity", nullable = false)
    @Min(value = 0)
    @Builder.Default
    private Integer quantity = 0;

    public OrderItem(Orders order, UUID menuId, String menuName, Integer price, Integer quantity) {
        this.order = order;
        this.menuId = menuId;
        this.menuName = menuName;
        this.price = price;
        this.quantity = quantity;
    }
}
