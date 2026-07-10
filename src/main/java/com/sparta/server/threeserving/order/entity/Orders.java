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
@Table(name = "p_order")
@NoArgsConstructor
public class Orders extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="user_id", nullable = false)
    private Long userId;

    @Column(name="store_id", nullable = false)
    private UUID storeId;

    @OneToOne
    @JoinColumn(name="cart_id", unique = true)
    private Cart cart;

    @Column(name="order_status", nullable = false)
    @Enumerated(value=EnumType.STRING)
    private OrderStatusEnum orderStatus;

    @Column(name="order_type", nullable = false)
    @Enumerated(value=EnumType.STRING)
    private OrderTypeEnum orderType;

    @Column(name="total_price", nullable = false)
    @Min(value = 0)
    private Integer totalPrice;

    @Column(name="delivery_address", nullable = false)
    private String deliveryAddress;

    @Column(name="request_message")
    private String requestMessage;

    public Orders(
            Long userId,
            UUID storeId,
            Cart cart,
            OrderStatusEnum orderStatus,
            Integer totalPrice,
            String deliveryAddress,
            String requestMessage) {
        this.userId = userId;
        this.storeId = storeId;
        this.cart = cart;
        this.orderStatus = orderStatus;
        // 온라인 주문만 취급하기 때문에
        this.orderType = OrderTypeEnum.ONLINE;
        this.totalPrice = totalPrice;
        this.deliveryAddress = deliveryAddress;
        this.requestMessage = requestMessage;
    }
  
    public void changeStatus(OrderStatusEnum currentStatus) {

        this.orderStatus = currentStatus;
    }

    public void modifyInfo(String reqMsg, String address) {
        if(reqMsg != null)
            this.requestMessage = reqMsg;
        if(address != null)
            this.deliveryAddress = address;
    }
}
