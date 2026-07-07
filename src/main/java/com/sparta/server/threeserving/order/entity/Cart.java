package com.sparta.server.threeserving.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name="p_cart")
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="user_id")
    private UUID userId;

    @Column(name="store_id")
    private UUID storeId;
}