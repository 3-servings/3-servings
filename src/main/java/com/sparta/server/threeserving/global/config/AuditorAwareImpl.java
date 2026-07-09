package com.sparta.server.threeserving.global.config;

import com.sparta.server.threeserving.auth.UserDetailsImpl;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // 비로그인(회원가입 등) 또는 인증 정보 없음 -> auditor 없음(null 저장)
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        // JWT 인증 시 principal은 UserDetailsImpl -> 사용자 PK(Long) 반환
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return Optional.ofNullable(userDetails.getUser().getId());
        }

        return Optional.empty();
    }
}
