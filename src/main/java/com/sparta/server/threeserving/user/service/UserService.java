package com.sparta.server.threeserving.user.service;

import com.sparta.server.threeserving.auth.redis.RedisService;
import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.user.dto.SignupRequest;
import com.sparta.server.threeserving.user.dto.UserResponse;
import com.sparta.server.threeserving.user.dto.UserUpdateRequest;
import com.sparta.server.threeserving.user.entity.User;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import com.sparta.server.threeserving.user.entity.UserStatus;
import com.sparta.server.threeserving.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // 토큰으로 내 정보 조회
    @Transactional
    public UserResponse getMyInfo(Long userId){
        User user = getActiveUser(userId);
        log.info("내 정보 조회 : id={}", userId);
        return UserResponse.from(user);
    }

    // UPDATE : 본인 프로필(닉네임/전화번호) 수정
    @Transactional
    public UserResponse updateProfile(Long userId, UserUpdateRequest request) {
        User user = getActiveUser(userId);

        // 닉네임이 실제로 바뀌는 경우에만 중복 검사 unique
        if (request.nickname() != null && !request.nickname().equals(user.getNickname()) && userRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        }

        String newNickname = request.nickname() != null ? request.nickname() : user.getNickname();
        String newPhone = request.phone() != null ? request.phone() : user.getPhone();
        user.updateProfile(newNickname, newPhone);

        log.info("프로필 수정 : id={}, nickname={}", userId, newNickname);
        return UserResponse.from(user);
    }


    // SEARCH : 회원 목록 관리자용. 정렬/페이지는 Pageable로 주입.
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(UserRoleEnum role, String keyword, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword;
        log.info("회원 검색 : role={}, keyword={}, page={}, size={}",
                role, kw, pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.search(role, kw, pageable).map(UserResponse::from);
    }










    // DELETE : 회원탈퇴
    @Transactional
    public void withdraw(Long userId, String rawPassword) {
        User user = getActiveUser(userId);

        //본인 확인: 패스워드 확인
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
        user.withdraw();
        redisService.delValue(userId); // refresh 정리
        log.info("회원탈퇴 완료 : id={}", userId);
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

    // 이메일 인증 아이디 찾기/비번 재설정 기능 구현 시 활성화
    //RedisService.getEmailCode/setEmailCode/delEmailCode 추가 필요
    // private void verifyEmailCode(String email, String code){
    //     String saved = redisService.getEmailCode(email);
    //     if (saved == null) throw new CustomException(ErrorCode.EMAIL_CODE_EXPIRED);
    //     if (!saved.equals(code)) throw new CustomException(ErrorCode.EMAIL_CODE_MISMATCH);
    // }





}
