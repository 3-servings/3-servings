package com.sparta.server.threeserving.global.common;

import com.sparta.server.threeserving.auth.UserDetailsImpl;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<Long> {

    private static final Long SYSTEM_USER_ID = 0L;

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 익명 사용자는 UserDetailsImpl로 걸러짐.
        if(authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl userDetails){
            return Optional.of(userDetails.getUser().getId());
        }

        return Optional.of(SYSTEM_USER_ID);
    }
}
