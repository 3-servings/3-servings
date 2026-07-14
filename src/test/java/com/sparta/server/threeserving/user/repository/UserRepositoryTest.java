package com.sparta.server.threeserving.user.repository;

import com.sparta.server.threeserving.global.config.AuditorAwareImpl;
import com.sparta.server.threeserving.global.config.JpaAuditingConfig;
import com.sparta.server.threeserving.user.entity.User;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, AuditorAwareImpl.class})
public class UserRepositoryTest {


    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp(){
        userRepository.save(User.create("customer01", "고객일","c1@test.com","PW", "01011112222", UserRoleEnum.CUSTOMER));

        userRepository.save(User.create("owner01", "사장일", "o1@test.com", "PW", "01011113333", UserRoleEnum.OWNER));

    }


    @Test
    @DisplayName("existsByUsername - 존재/미존재")
    void existsByUsername(){
        assertThat(userRepository.existsByUsername("customer01")).isTrue();
        assertThat(userRepository.existsByUsername("nobody")).isFalse();
    }

    @Test
    @DisplayName("search - 역할 필터")
    void search_by_role(){
        Page<User> result = userRepository.search(
                UserRoleEnum.OWNER, null, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("owner01");
    }

    @Test
    @DisplayName("search - 키워드(닉네임) 필터")
    void search_by_keyword(){
        Page<User> result = userRepository.search(
                null, "고객", PageRequest.of(0,10,Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNickname()).isEqualTo("고객일");
    }












}
