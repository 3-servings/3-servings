package com.sparta.server.threeserving.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

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

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    //소프트 딜리트 : 실제 삭제 X
    //레디스 사용이라 리프레시토큰 DB에 저장안함 그런고로 필드 구현 X

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }


    // 비즈니스 로직
    public void changePassword(String password){
        this.password = password;
    }

    public void changeNickname(String nickname){
        this.nickname = nickname;
    }
    public void changePhone(String phone){
        this.phone = phone;
    }

    //회원탈퇴시에
    //유니크 속성 컬럼 중복 방지 메서드
    public void withdraw(){
        String suffix = "__deleted__" + UUID.randomUUID();

        this.status = UserStatus.DELETED;
        //삭제 시간 Auditing BaseEntity 상속 시에 주석 풀기
        //this.deletedAt = LocalDateTime.now();

        this.email += suffix;
        this.username += suffix;
        this.nickname += suffix;
    }

    public void block(){
        this.status = UserStatus.BLOCKED;
    }


}