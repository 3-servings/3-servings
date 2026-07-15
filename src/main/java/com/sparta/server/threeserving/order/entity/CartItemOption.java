package com.sparta.server.threeserving.order.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="p_cart_item_option")
@EntityListeners(AuditingEntityListener.class)
@Builder
public class CartItemOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "cart_item_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private CartItem cartItem;

    @Column(name="option_item_id")
    private UUID optionItemId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    public CartItemOption(CartItem cartItem, UUID optionItemId) {
        this.cartItem = cartItem;
        this.optionItemId = optionItemId;
    }
}