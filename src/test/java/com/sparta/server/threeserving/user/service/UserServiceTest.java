package com.sparta.server.threeserving.user.service;

import com.sparta.server.threeserving.auth.redis.RedisService;
import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.user.dto.SignupRequest;
import com.sparta.server.threeserving.user.dto.UserResponse;
import com.sparta.server.threeserving.user.entity.User;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import com.sparta.server.threeserving.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    RedisService redisService;
    @InjectMocks UserService userService;

    private SignupRequest signupRequest(String username){
        SignupRequest req = new SignupRequest();

        ReflectionTestUtils.setField(req, "username", username);
        ReflectionTestUtils.setField(req, "email", "test@test.com");
        ReflectionTestUtils.setField(req, "nickname", "닉네임");
        ReflectionTestUtils.setField(req, "password", "Test1234!@##");
        ReflectionTestUtils.setField(req, "phone", "01011112222");
        return req;
    }


    @Test
    @DisplayName("회원가입 성공")
    void signup_success(){
        SignupRequest req = signupRequest("customer01");
        given(userRepository.existsByUsername(anyString())).willReturn(false);
        given(userRepository.existsByNickname(anyString())).willReturn(false);
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("ENCODED");
        //given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArguments(0));

        UserResponse res = userService.signup(req, UserRoleEnum.CUSTOMER);

        assertThat(res.getUsername()).isEqualTo("customer01");
        assertThat(res.getRole()).isEqualTo("CUSTOMER");
        verify(userRepository).save(any(User.class));

    }


    @Test
    @DisplayName("회원가입 실패 - 아이디 중복")
    void signup_fail_duplicated_username(){
        SignupRequest req = signupRequest("customer01");
        given(userRepository.existsByUsername("customer01")).willReturn(true);

        assertThatThrownBy(() -> userService.signup(req, UserRoleEnum.CUSTOMER))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USERNAME_DUPLICATED);
    }

    @Test
    @DisplayName("회원탈퇴 실패 - 비밀번호 불일치")
    void withdraw_fail_invalid_password(){
        User user = User.create("customer01", "닉네임", "test@test.com", "ENCODED", "01011112222", UserRoleEnum.CUSTOMER);

        ReflectionTestUtils.setField(user, "id", 1L);

        given(userRepository.findById(1L)).willReturn(java.util.Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        assertThatThrownBy(() -> userService.withdraw(1L, "wrong"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PASSWORD);
    }















}
