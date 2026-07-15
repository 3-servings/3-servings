package com.sparta.server.threeserving.auth.kakao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.server.threeserving.auth.cookie.CookieUtil;
import com.sparta.server.threeserving.auth.jwt.JwtUtil;
import com.sparta.server.threeserving.auth.redis.RedisService;
import com.sparta.server.threeserving.user.dto.LoginResult;
import com.sparta.server.threeserving.user.entity.User;
import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import com.sparta.server.threeserving.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KakaoService {

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.rest-api-key}")
    private String restApiKey;

    private final Set<UserRoleEnum> ALLOWED_ROLES = Set.of(UserRoleEnum.CUSTOMER,UserRoleEnum.OWNER);

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;
    private final RedisService redisService;

    public String getAuthorizeUrl(String role) {
        return "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + restApiKey
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&state=" + role;
    }

    public LoginResult login(String code, String role) throws JsonProcessingException{
        // 1. "인가 코드"로 "액세스 토큰" 요청
        String kakaoAccessToken = requestKakaoToken(code);

        // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보"가져오기
        KakaoUserInfo kakaoUserInfo = getKakaoUserInfo(kakaoAccessToken);

        // 3. 필요시에 회원 가입
        String encodeRandomPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        User user = userRepository.findByProviderId("kakao_" + kakaoUserInfo.getId())
                .orElseGet(() -> userRepository.save(
                        User.createSocial(
                                "kakao_" + kakaoUserInfo.getId(),
                                kakaoUserInfo.getNickname(),
                                kakaoUserInfo.getEmail(),
                                "KAKAO",
                                "kakao_" +kakaoUserInfo.getId(),
                                encodeRandomPassword,
                                resolveRole(role)
                        )
                ));

        String accessToken = jwtUtil.createAccessToken(user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.createRefreshToken(user.getId(), user.getRole());

        redisService.setValueTtl(user.getId(),refreshToken,14);

        return new LoginResult(accessToken, refreshToken);
    }

    private KakaoUserInfo getKakaoUserInfo(String kakaoAccessToken) throws JsonProcessingException {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("https://kapi.kakao.com")
                .path("/v2/user/me")
                .encode()
                .build()
                .toUri();

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + kakaoAccessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(uri)
                .headers(headers)
                .body(new LinkedMultiValueMap<>());

        // HTTP 요청 보내기
        ResponseEntity<String> response = restTemplate.exchange(
                requestEntity,
                String.class
        );

        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        Long id = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties")
                .get("nickname").asText();
        String email = jsonNode.get("kakao_account")
                .get("email").asText();


        return new KakaoUserInfo(id, nickname, email);

    }

    private String requestKakaoToken(String code) throws JsonProcessingException {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com")
                .path("/oauth/token")
                .encode()
                .build()
                .toUri();

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        //HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", restApiKey);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);
        body.add("client_secret", clientId);

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(uri)
                .headers(headers)
                .body(body);

        // HTTP 요청 보내기
        ResponseEntity<String> response = restTemplate.exchange(
                requestEntity,
                String.class
        );

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        return jsonNode.get("access_token").asText();
    }

    private UserRoleEnum resolveRole(String state) {
        try {
            UserRoleEnum role = UserRoleEnum.valueOf(state);
            return ALLOWED_ROLES.contains(role) ? role : UserRoleEnum.CUSTOMER;
        } catch (Exception e) {
            return UserRoleEnum.CUSTOMER;   // state 없거나 이상한 값이면 기본 CUSTOMER
        }
    }
}
