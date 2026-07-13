package com.sparta.server.threeserving.user.service;

import com.sparta.server.threeserving.auth.redis.RedisService;
import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.user.dto.SignupRequest;
import com.sparta.server.threeserving.user.dto.UserResponse;
import com.sparta.server.threeserving.user.entity.User;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import com.sparta.server.threeserving.user.entity.UserStatus;
import com.sparta.server.threeserving.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RedisService redisService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisService = redisService;
    }

    //고객/사장 회원 가입
    @Transactional
    public UserResponse signup(SignupRequest request, UserRoleEnum role) {

        validateDuplicate(request);

        User user = User.create(
                request.getUsername(),
                request.getNickname(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhone(),
                role   // 컨트롤러에서 넘어온 서버 상수
        );

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.DUPLICATED_RESOURCE);
        }

        log.info("회원가입 완료 : id={}, username={}, role={}",
                user.getId(),
                user.getUsername(),
                user.getRole()
        );
        return UserResponse.from(user);
    }

    @Transactional
    public void withdraw(Long userId, String rawPassword){
        User user = getActiveUser(userId);

        // 본인 확인
        if (!passwordEncoder.matches(rawPassword, user.getPassword())){
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
        user.withdraw();
        redisService.delValue(userId); // refresh 정리
    }

    // 활성 회원 조회 : 없으면 USER_NOT_FOUND, 이미 탈퇴면 ALREADY_WITHDRAWN
    private User getActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new CustomException(ErrorCode.ALREADY_WITHDRAWN);
        }
        return user;
    }


    private void validateDuplicate(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(ErrorCode.USERNAME_DUPLICATED);
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_DUPLICATED);
        }
    }

    // TODO: 이메일 인증(아이디 찾기/비번 재설정) 기능 구현 시 활성화
    //  - RedisService.getEmailCode/setEmailCode/delEmailCode 추가 필요
    // private void verifyEmailCode(String email, String code){
    //     String saved = redisService.getEmailCode(email);
    //     if (saved == null) throw new CustomException(ErrorCode.EMAIL_CODE_EXPIRED);
    //     if (!saved.equals(code)) throw new CustomException(ErrorCode.EMAIL_CODE_MISMATCH);
    // }





}
