package com.sparta.server.threeserving.auth.jwt;


import com.sparta.server.threeserving.auth.cookie.CookieUtil;
import com.sparta.server.threeserving.auth.redis.RedisService;
import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.user.entity.User;
import com.sparta.server.threeserving.user.repository.UserRepository;
import com.sparta.server.threeserving.user.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final UserRepository userRepository;

    public String reissueAccessToken(HttpServletRequest request){
        Cookie cookie = CookieUtil.getCookie(request, "refreshToken")
                .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        String token = jwtUtil.substringToken(cookie.getValue());

        if(!jwtUtil.validateToken(token)){
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Claims claims = jwtUtil.getUserInfoFromToken(token);

        Long userId = Long.valueOf(claims.getSubject());

        if(!validateRefreshToken(userId, token)){
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        return jwtUtil.createAccessToken(user.getUsername(), user.getRole());
    }

    public boolean validateRefreshToken(Long userId, String token){
        String saveRefreshToken = redisService.getValue(userId);

        return saveRefreshToken.equals(token);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = CookieUtil.getCookie(request, "refreshToken").orElseThrow(
                () -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND)
        );

        String token = jwtUtil.substringToken(cookie.getValue());

        Claims claims = jwtUtil.getUserInfoFromToken(token);

        Long userId = Long.valueOf(claims.getSubject());

        redisService.delValue(userId);

        CookieUtil.deleteCookie(request, response, "refreshToken");
    }
}
