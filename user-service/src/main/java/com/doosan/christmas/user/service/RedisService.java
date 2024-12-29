package com.doosan.christmas.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {
    
    private final StringRedisTemplate redisTemplate;
    private static final long VERIFICATION_TIME = 300L; // 5분
    private static final String REFRESH_TOKEN_PREFIX = "RT:";
    private static final String BLACKLIST_PREFIX = "BL:";
    
    public void saveEmailVerification(String email, String code) {
        log.debug("[Redis 저장] 인증코드 캐시 저장 - 이메일: {}, 인증코드: {}, 유효시간: {}초",
                email, code, VERIFICATION_TIME);
        redisTemplate.opsForValue().set(email, code, VERIFICATION_TIME, TimeUnit.SECONDS);
        log.info("[Redis 저장] 인증코드 저장 완료 - 이메일: {}", email);
    }
    
    public String getEmailVerification(String email) {
        return redisTemplate.opsForValue().get(email);
    }
    
    public void setVerifiedEmail(String email) {
        redisTemplate.opsForValue().set(email + ":verified", "true", 24, TimeUnit.HOURS);
    }
    
    public boolean isEmailVerified(String email) {
        String value = redisTemplate.opsForValue().get(email + ":verified");
        return "true".equals(value);
    }
    
    public void saveRefreshToken(String email, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + email;
        redisTemplate.opsForValue().set(key, refreshToken);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
    }
    
    public String getRefreshToken(String email) {
        return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + email);
    }
    
    public void deleteRefreshToken(String email) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + email);
    }
    
    public void addToBlacklist(String accessToken, long expiration) {
        String key = BLACKLIST_PREFIX + accessToken;
        redisTemplate.opsForValue().set(key, "logout");
        redisTemplate.expire(key, expiration, TimeUnit.MILLISECONDS);
    }
    
    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken));
    }
} 