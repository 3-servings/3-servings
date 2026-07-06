package com.sparta.server.threeserving.order.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import jakarta.persistence.*;
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

    @Column(name="cart_id")
    private UUID cartId;

    @Column(name="menu_id")
    private UUID menuId;

    @Column(name="quantity")
    private Integer quantity;
}