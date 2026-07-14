package com.sparta.server.threeserving.global.config;

import com.sparta.server.threeserving.auth.UserDetailsServiceImpl;
import com.sparta.server.threeserving.auth.jwt.JwtAuthenticationFilter;
import com.sparta.server.threeserving.auth.jwt.JwtAuthorizationFilter;
import com.sparta.server.threeserving.auth.jwt.JwtUtil;
import com.sparta.server.threeserving.auth.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) //CORS 허용
            .formLogin(form -> form.disable())   //Spring Security 기본 로그인 페이지 비활성화
            .httpBasic(basic -> basic.disable()) // Basic 인증 비활성화
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // ===== 인가 규칙 템플릿 =====
                    // 규칙은 위에서부터 순서대로 평가되어 먼저 매칭되는 규칙이 적용됩니다.

                    // user (회원 리소스)
                    .requestMatchers(HttpMethod.GET, "/api/users").hasAnyRole("MASTER", "MANAGER")//회원 검색: 관리자만
                    //내 정보 조회/수정: 로그인 필요
                    .requestMatchers("/api/users/**").authenticated()
                    // 공개 API (인증 불필요)
                    .requestMatchers("/signup").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()

                    // Order
                    .requestMatchers("/api/carts/**").hasAnyRole("CUSTOMER", "MASTER", "MANAGER")

                    .requestMatchers(HttpMethod.POST, "/api/orders").hasAnyRole("MASTER", "MANAGER")
                    .requestMatchers(HttpMethod.GET, "/api/orders/{orderId}").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/orders").authenticated()
                    .requestMatchers(HttpMethod.PATCH, "/api/orders/{orderId}").hasAnyRole("CUSTOMER", "MASTER", "MANAGER")
                    .requestMatchers(HttpMethod.PATCH, "/api/orders/{orderId}/cancel").hasAnyRole("CUSTOMER", "MASTER", "MANAGER")
                    .requestMatchers(HttpMethod.DELETE, "/api/orders/{orderId}").hasAnyRole("MASTER", "MANAGER")

                    // Store
                    .requestMatchers(HttpMethod.GET, "/api/stores/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/stores/**").hasRole("OWNER")
                    .requestMatchers(HttpMethod.PUT, "/api/stores/**").hasRole("OWNER")
                    .requestMatchers(HttpMethod.PATCH, "/api/stores/**").hasRole("OWNER")
                    .requestMatchers(HttpMethod.DELETE, "/api/stores/**").hasAnyRole("OWNER", "MASTER", "MANAGER")

                    .requestMatchers(HttpMethod.GET, "/api/regions/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/regions/**").hasAnyRole("MASTER", "MANAGER")
                    .requestMatchers(HttpMethod.PUT, "/api/regions/**").hasAnyRole("MASTER", "MANAGER")
                    .requestMatchers(HttpMethod.PATCH, "/api/regions/**").hasAnyRole("MASTER", "MANAGER")
                    .requestMatchers(HttpMethod.DELETE, "/api/regions/**").hasAnyRole("MASTER", "MANAGER")

                    .requestMatchers(HttpMethod.GET, "/api/categorys/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/categorys/**").hasAnyRole("MASTER", "MANAGER")
                    .requestMatchers(HttpMethod.PUT, "/api/categorys/**").hasAnyRole("MASTER", "MANAGER")
                    .requestMatchers(HttpMethod.PATCH, "/api/categorys/**").hasAnyRole("MASTER", "MANAGER")
                    .requestMatchers(HttpMethod.DELETE, "/api/categorys/**").hasAnyRole("MASTER", "MANAGER")
                    // .requestMatchers(HttpMethod.GET, "/api/stores/**").permitAll()
                    // .requestMatchers(HttpMethod.POST, "/api/stores/**").hasRole("OWNER")


                    // Menu


                    // OrderManagement

                    // Payment
                    .requestMatchers(HttpMethod.POST, "/api/orders/*/payments").hasRole("CUSTOMER")
                    .requestMatchers(HttpMethod.POST, "/api/orders/*/payments/confirm").hasRole("CUSTOMER")
                    .requestMatchers(HttpMethod.PATCH, "/api/orders/*/payments/refund").hasRole("CUSTOMER")
                    .requestMatchers(HttpMethod.GET, "/api/orders/*/payments/**").authenticated()

                    // review
                    .requestMatchers(HttpMethod.GET, "/api/reviews").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/reviews/*").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/stores/*/reviews").permitAll()
                    .requestMatchers("/api/reviews/**").authenticated()


                    // ⚠️ 임시: 위 도메인 규칙이 채워지기 전까지 나머지는 모두 허용.
                    // 팀 합의 후 permitAll() -> authenticated()로 변경
                    .anyRequest().permitAll()
        );

        // 필터 관리
        http.addFilterBefore(jwtAuthorizationFilter(), JwtAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "https://threeservings.site/",
                "http://localhost:3000" //로컬 확인용
        ));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
