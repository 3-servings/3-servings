package com.sparta.server.threeserving.order.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "p_order_item")
@NoArgsConstructor
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
    private Integer price;

    @Column(name="quantity", nullable = false)
    private Integer quantity;

    public OrderItem(Orders order, UUID menuId, String menuName, Integer price, Integer quantity) {
        this.order = order;
        this.menuId = menuId;
        this.menuName = menuName;
        this.price = price;
        this.quantity = quantity;
    }
}
