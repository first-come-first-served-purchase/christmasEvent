package com.doosan.userservice.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisUtil {

    private final StringRedisTemplate redisTemplate;

    public void setDataExpire(String key, String value, long duration) {
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(duration));
        log.info("Redis 데이터 저장: key={}, value={}, expire={}초", key, value, duration);
    }

    public void setData(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
        log.info("Redis 데이터 저장: key={}, value={}", key, value);
    }

    public String getData(String key) {
        String value = redisTemplate.opsForValue().get(key);
        log.info("Redis 데이터 조회: key={}, value={}", key, value);
        return value;
    }

    public void deleteData(String key) {
        boolean deleted = Boolean.TRUE.equals(redisTemplate.delete(key));
        if (deleted) {
            log.info("Redis 데이터 삭제 성공: key={}", key);
        } else {
            log.warn("Redis 데이터 삭제 실패 또는 존재하지 않음: key={}", key);
        }
    }
}
