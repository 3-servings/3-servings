package com.sparta.server.threeserving.global.config;

import com.sparta.server.threeserving.auth.cookie.CookieUtil;
import com.sparta.server.threeserving.auth.jwt.JwtAuthenticationFilter;
import com.sparta.server.threeserving.auth.jwt.JwtAuthorizationFilter;
import com.sparta.server.threeserving.auth.jwt.JwtUtil;
import com.sparta.server.threeserving.auth.UserDetailsServiceImpl;
import com.sparta.server.threeserving.auth.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationConfiguration authenticationConfiguration;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, redisService);
        filter.setAuthenticationManager(authenticationManager(authenticationConfiguration));
        return filter;
    }

    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter(jwtUtil, userDetailsService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())   //Spring Security 기본 로그인 페이지 비활성화
            .httpBasic(basic -> basic.disable()) // Basic 인증 비활성화
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // ===== 인가 규칙 템플릿 =====
                    // 규칙은 위에서부터 순서대로 평가되어 먼저 매칭되는 규칙이 적용됩니다.

                    // 공개 API (인증 불필요)
                    .requestMatchers("/signup").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()

                    // Order 예시. 실제 권한 확정 후 채워넣기
                    // .requestMatchers("/api/carts/**").hasRole("CUSTOMER")
                    // .requestMatchers("/api/carts/**", "/api/carts").hasRole("CUSTOMER")
                    // .requestMatchers(HttpMethod.GET, "/api/stores/**").permitAll()
                    // .requestMatchers(HttpMethod.POST, "/api/stores/**").hasRole("OWNER")

                    // Store


                    // Menu


                    // OrderManagement

                    // Payment

                    // review



                    // ⚠️ 임시: 위 도메인 규칙이 채워지기 전까지 나머지는 모두 허용.
                    // 팀 합의 후 permitAll() -> authenticated()로 변경
                    .anyRequest().permitAll()
        );

        // 필터 관리
        http.addFilterBefore(jwtAuthorizationFilter(), JwtAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

}
