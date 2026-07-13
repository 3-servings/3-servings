package com.sparta.server.threeserving.user.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "p_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;



    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    @Builder.Default
    private UserRoleEnum role = UserRoleEnum.CUSTOMER;// 기본 회원 권한 CUSTOMER

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LoginType loginType = LoginType.LOCAL; // 기본 로그인타입 LOCAL, 소셜로그인타입 KAKAO

    @Column(length = 255)
    private String provider;

    @Column(length = 255)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column
    private String phone;


    public static User create(
            String username,
            String nickname,
            String email,
            String encodedPassword,
            String phone,
            UserRoleEnum role
    ) {
        return User.builder()
                .username(username)
                .nickname(nickname)
                .email(email)
                .password(encodedPassword)
                .phone(phone)
                .role(role)
                .build();
    }


    //소프트 딜리트 : 실제 삭제 X
    //레디스 사용이라 리프레시토큰 DB에 저장안함 그런고로 필드 구현 X


    // 비즈니스 로직
    public void updateProfile(String nickname, String phone) {
        this.nickname = nickname;
        this.phone = phone;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    //회원탈퇴시에
    //유니크 속성 컬럼 중복 방지 메서드
    public void withdraw(){
        if (this.status == UserStatus.DELETED) {
            return;
        }

        String suffix = "__deleted__" + UUID.randomUUID();

        this.status = UserStatus.DELETED;
        //삭제 시간 Auditing BaseEntity 상속 시에 주석 풀기
        //this.deletedAt = LocalDateTime.now();

        this.email += suffix;
        this.username += suffix;
        this.nickname += suffix;
        softDelete(this.id); // BaseEntity deletedAt/deleteBy 세팅
    }

    public void block(){
        this.status = UserStatus.BLOCKED;
    }


}