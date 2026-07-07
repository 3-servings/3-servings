package com.sparta.server.threeserving.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name="p_cart_item")
public class CartItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="cart_id", nullable = false)
    private UUID cartId;

    @Column(name="menu_id", nullable = false)
    private UUID menuId;

    @Column(name="quantity", nullable = false)
    @Min(value = 1)
    private Integer quantity;
}