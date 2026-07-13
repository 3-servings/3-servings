package com.sparta.server.threeserving.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.server.threeserving.auth.UserDetailsImpl;
import com.sparta.server.threeserving.auth.cookie.CookieUtil;
import com.sparta.server.threeserving.auth.redis.RedisService;
import com.sparta.server.threeserving.user.dto.LoginRequestDto;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.Duration;

@Slf4j(topic = "로그인 및 JWT 생성")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private static final int REFRESH_TOKEN_EXPIRE_DAYS = 14;
    private static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(REFRESH_TOKEN_EXPIRE_DAYS);
    private final JwtUtil jwtUtil;

    private final RedisService redisService;


    public JwtAuthenticationFilter(JwtUtil jwtUtil, RedisService redisService) {
        this.jwtUtil = jwtUtil;
        this.redisService = redisService;
        setFilterProcessesUrl("/api/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            LoginRequestDto requestDto = new ObjectMapper().readValue(request.getInputStream(), LoginRequestDto.class);

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getUsername(),
                            requestDto.getPassword(),
                            null
                    )
            );
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authResult.getPrincipal();

        Long userId = userDetails.getUser().getId();
        String username = userDetails.getUsername();
        UserRoleEnum role = userDetails.getUser().getRole();
        String accessToken = jwtUtil.createAccessToken(username, role);
        String refreshToken = jwtUtil.createRefreshToken(userId, role);

        //redis에 저장
        redisService.setValueTtl(userId, refreshToken, REFRESH_TOKEN_EXPIRE_DAYS);

        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, accessToken);
        CookieUtil.addCookie(response,"refreshToken", refreshToken, (int) REFRESH_TOKEN_DURATION.getSeconds());
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(401);
    }

}