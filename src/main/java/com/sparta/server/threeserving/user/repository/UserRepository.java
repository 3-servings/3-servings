package com.sparta.server.threeserving.user.repository;

import com.sparta.server.threeserving.user.entity.User;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByProviderId(String id);

    // 회원 검색: 역할(선택) + 키워드(username/nickname, 선택). 탈퇴 회원 제외. 정렬/페이지는 Pageable로 주입.
    @Query("SELECT u FROM User u " +
            "WHERE u.deletedAt IS NULL " +
            "AND (:role IS NULL OR u.role = :role) " +
            "AND (:keyword IS NULL OR u.username LIKE %:keyword% OR u.nickname LIKE %:keyword%)")
    Page<User> search(@Param("role") UserRoleEnum role,
                       @Param("keyword") String keyword,
                       Pageable pageable);
}
