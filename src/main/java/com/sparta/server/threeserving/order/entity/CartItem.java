package com.sparta.server.threeserving.order.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="p_cart_item")
@Builder
public class CartItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "cart_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Cart cart;

    @Column(name="menu_id", nullable = false)
    private UUID menuId;

    @Column(name="quantity", nullable = false)
    @Min(value = 1)
    private Integer quantity;

    public CartItem(Cart cart, UUID menuId, Integer quantity) {
        this.cart = cart;
        this.menuId = menuId;
        this.quantity = quantity;
    }
}