package com.sparta.server.threeserving.global.config;

import com.sparta.server.threeserving.auth.UserDetailsServiceImpl;
import com.sparta.server.threeserving.auth.jwt.*;
import com.sparta.server.threeserving.auth.jwt.*;
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
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

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
            .exceptionHandling(handler -> handler
                    .authenticationEntryPoint(customAuthenticationEntryPoint)
                    .accessDeniedHandler(customAccessDeniedHandler))
            .authorizeHttpRequests(auth -> auth
                    // ===== 인가 규칙 템플릿 =====
                    // 규칙은 위에서부터 순서대로 평가되어 먼저 매칭되는 규칙이 적용됩니다.

                    // user (회원 리소스)
                    .requestMatchers(HttpMethod.GET, "/api/users").hasAnyRole("MASTER", "MANAGER")//회원 검색: 관리자만
                    //내 정보 조회/수정: 로그인 필요
                    .requestMatchers("/api/users/user/kakao/login", "/api/users/kakao/call-back").permitAll()
                    .requestMatchers("/api/users/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/auth/delete").authenticated()
                    // 공개 API (인증 불필요)
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


                    // Menu
                    .requestMatchers(HttpMethod.POST, "/api/stores/{storeId}/menu-categories").hasAnyRole("OWNER", "MASTER")
                    .requestMatchers(HttpMethod.GET, "/api/stores/{storeId}/menu-categories").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/menu-categories/{menuCategoryId}").hasAnyRole("OWNER", "MASTER")
                    .requestMatchers(HttpMethod.PATCH, "/api/stores/{storeId}/menu-categories/display-order").hasAnyRole("OWNER", "MASTER")
                    .requestMatchers(HttpMethod.DELETE, "/api/menu-categories/{menuCategoryId}").hasAnyRole("OWNER", "MASTER")

                    .requestMatchers(HttpMethod.POST, "/api/stores/{storeId}/option-groups").hasAnyRole("OWNER", "MASTER")
                    .requestMatchers(HttpMethod.GET, "/api/stores/{storeId}/option-groups").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/option-groups/{optionGroupId}").hasAnyRole("OWNER", "MASTER")
                    .requestMatchers(HttpMethod.DELETE, "/api/option-groups/{optionGroupId}").hasAnyRole("OWNER", "MASTER")
                    .requestMatchers(HttpMethod.PUT, "/api/menus/{menuId}/option-groups").hasAnyRole("OWNER", "MASTER")
                    .requestMatchers(HttpMethod.PATCH, "/api/stores/{storeId}/option-items/status").hasAnyRole("OWNER", "MASTER")

                    .requestMatchers(HttpMethod.POST, "/api/stores/{storeId}/menus").hasAnyRole("OWNER", "MASTER")
                    .requestMatchers(HttpMethod.GET, "/api/stores/{storeId}/menus").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/stores/{storeId}/menu-board").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/menus/{menuId}").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/menus/{menuId}").hasAnyRole("OWNER", "MASTER")
                    .requestMatchers(HttpMethod.DELETE, "/api/menus/{menuId}").hasAnyRole("OWNER", "MASTER")
                    .requestMatchers(HttpMethod.PATCH, "/api/stores/{storeId}/menus/status").hasAnyRole("OWNER", "MASTER")
                    .requestMatchers(HttpMethod.PATCH, "/api/stores/{storeId}/menus/display-order").hasAnyRole("OWNER", "MASTER")

                    // Image
                    .requestMatchers("/api/images/presigned-url").hasAnyRole("OWNER", "MASTER")

                    // Ai
                    .requestMatchers(HttpMethod.POST, "/api/ai/description").hasAnyRole("OWNER", "MASTER")

                    // OrderManagement
                    .requestMatchers(HttpMethod.GET, "/api/order-management/**").hasAnyRole("OWNER", "MASTER")
                    .requestMatchers(HttpMethod.PATCH, "/api/order-management/**").hasAnyRole("OWNER", "MASTER")
                    .requestMatchers(HttpMethod.POST, "/api/order-management/orders/reject-reason-codes").hasAnyRole( "MASTER")


                    // Payment
                    .requestMatchers(HttpMethod.POST, "/api/orders/*/payments").hasRole("CUSTOMER")
                    .requestMatchers(HttpMethod.POST, "/api/orders/*/payments/toss/confirm").hasRole("CUSTOMER")
                    .requestMatchers(HttpMethod.PATCH, "/api/orders/*/payments/refund").hasRole("CUSTOMER")
                    .requestMatchers(HttpMethod.PATCH, "/api/orders/*/payments/toss/refund").hasRole("CUSTOMER")
                    .requestMatchers(HttpMethod.GET, "/api/orders/*/payments/**").authenticated()

                    // review
                    .requestMatchers(HttpMethod.GET, "/api/reviews").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/reviews/*").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/stores/*/reviews").permitAll()
                    .requestMatchers("/api/reviews/**").authenticated()

                    // 팀 합의 후 permitAll() -> authenticated()로 변경
                    .anyRequest().authenticated()
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
