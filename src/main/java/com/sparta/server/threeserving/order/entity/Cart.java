package com.sparta.server.threeserving.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name="p_cart")
@NoArgsConstructor
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="user_id")
    private Long userId;

    @Column(name="store_id")
    private UUID storeId;

    @JoinColumn(name = "cart_id", nullable = false)
    @OneToMany
    private List<CartItem> cartItems;

    public Cart(Long userId, UUID storeId){
        this.userId = userId;
        this.storeId = storeId;
    }
}