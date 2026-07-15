package com.sparta.server.threeserving.order.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Getter
@Entity
@Table(name = "p_order")
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Builder.Default
    private Cart cart = null;

    @Column(name="order_status", nullable = false)
    @Enumerated(value=EnumType.STRING)
    private OrderStatusEnum orderStatus;

    @Column(name="order_type", nullable = false)
    @Enumerated(value=EnumType.STRING)
    private OrderTypeEnum orderType;

    @Column(name="total_price", nullable = false)
    @Min(value = 0)
    @Builder.Default
    private Integer totalPrice = 0;

    @Column(name="delivery_address", nullable = false)
    @NotBlank
    private String deliveryAddress;

    @Column(name="request_message")
    @Builder.Default
    private String requestMessage = "";

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
  
    public void changeStatus(OrderStatusEnum nextStatus) {
        validateStatusTransition(nextStatus);
        this.orderStatus = nextStatus;
    }

    public void modifyInfo(String reqMsg, String address) {
        if(reqMsg != null)
            this.requestMessage = reqMsg;
        if(address != null)
            this.deliveryAddress = address;
    }

    private void validateStatusTransition(OrderStatusEnum status) {
        if (!this.orderStatus.canTransitionTo(status)) {
            throw new CustomException(ErrorCode.ORDER_STATUS_TRANSITION_INVALID);
        }
    }
}
