package com.sparta.server.threeserving.review.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import com.sparta.server.threeserving.order.entity.Orders;
import com.sparta.server.threeserving.store.entity.Store;
import com.sparta.server.threeserving.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private int star;   // 별점 1~5

    @Column(nullable = true)
    private String content; // 리뷰내용 (null 허용)

    @Builder
    private Review(Orders order,
                  User user,
                  Store store,
                  int star,
                  String content
    ) {
        this.order = order;
        this.user = user;
        this.store = store;
        this.star = star;
        this.content = content;
    }

    public static Review create(Orders order,
                                User user,
                                Store store,
                                int star,
                                String content
    ){
        return Review.builder()
                .order(order)
                .user(user)
                .store(store)
                .star(star)
                .content(content)
                .build();
    }

    //별점 내용 수정
    public void update(int star, String content){
        this.star = star;
        this.content = content;
    }

    //작성자 본인 여부
    public boolean isOwner(Long userId){
        return this.user.getId().equals(userId);
    }
}
