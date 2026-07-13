package com.sparta.server.threeserving.review.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import com.sparta.server.threeserving.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewComment extends BaseEntity {
    /**
     * 사장 답글 테이블 (고객 리뷰에 대한 대댓글입니다)
     * 리뷰 1개당 답글 1개 (review_id 참조 FK 1:1
     * 작성자(User)는 해당 가게의 사장(owner)
     */

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false, unique = true)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 답글 작성 사장

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 답글 내용

    @Builder
    private ReviewComment(Review review, User user, String content){
        this.review = review;
        this.user = user;
        this.content = content;
    }

    public static ReviewComment create(Review review, User user, String content){
        return ReviewComment.builder()
                .review(review)
                .user(user)
                .content(content)
                .build();
    }

    public void update(String content) {          // 답글 수정
        this.content = content;
    }

    public boolean isOwner(Long userId) {         // 답글 작성자 본인 여부
        return this.user.getId().equals(userId);
    }
}
