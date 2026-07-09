package com.sparta.server.threeserving.auth.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public void setValue(Long userId, String value){
        String key = "refreshToken:" + userId;
        redisTemplate.opsForValue().set(key, value);
    }

    public String getValue(Long userId){
        String key = "refreshToken:" + userId;
        return redisTemplate.opsForValue().get(key);
    }

    public void setValueTtl(Long userId, String refreshToken, int day){
        String key = "refreshToken:" + userId;
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofDays(day));
    }

    public void delValue(Long userId){
        String key = "refreshToken:" + userId;
        redisTemplate.delete(key);
    }
}
